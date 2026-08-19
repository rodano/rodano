#!/usr/bin/env python3
"""Rodano release helper.

Implements the Git-flow release process for the Rodano monorepo. The Git tag is
the trigger for the CI workflows (GitHub release, documentation deployment and
versioned Docker images), while the version strings inside pom.xml and the
package.json/package-lock.json files remain authoritative for the published
Maven and npm artifacts.

Procedures:
    branch  X.Y.Z   Cut releases/vX.Y.Z from dev and bump files to X.Y.Z-SNAPSHOT.
    rc      X.Y.Z   Bump files to the next X.Y.Z-rc.N and tag vX.Y.Z-rcN.
    release X.Y.Z   Bump files to X.Y.Z, merge into main, tag vX.Y.Z, back-merge
                    into dev and delete the release branch.
    cleanup X.Y.Z   Delete the release branch and the RC tags for X.Y.Z.

dev never carries a semantic version: pom.xml always reads DEV-SNAPSHOT and the
frontends' package.json/package-lock.json always read 0.0.0-dev; the actual
next version is only decided when "branch" is run.

Global flags:
    --dry-run   Run every check and print the commands without mutating anything.
    --yes       Skip the interactive confirmation prompt.
"""

import argparse
import logging
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent

# Files carrying a version string that must be kept in sync with the release.
POM = ROOT / 'backend' / 'pom.xml'
FRONTENDS = ('configurator', 'epro', 'main')

# A release version is a plain semantic version without pre-release identifier.
VERSION_RE = re.compile(r'^\d+\.\d+\.\d+$')

DEV_BRANCH = 'dev'
MAIN_BRANCH = 'main'
REMOTE = 'origin'


def release_branch(version):
	"""Return the release branch name for a version (e.g. releases/v7.0.0)."""
	return f'releases/v{version}'


# Placeholder versions kept on dev at all times; "branch" is what decides the
# actual next semantic version. package.json requires a semver-shaped string,
# unlike the POM, so the frontends get their own placeholder.
DEV_POM_VERSION = 'DEV-SNAPSHOT'
DEV_FRONTEND_VERSION = '0.0.0-dev'


class ReleaseError(Exception):
	"""Raised when a pre-flight guard fails or a step cannot be completed."""


# Global flag toggled by the CLI; when set, mutating commands are only printed.
DRY_RUN = False

logger = logging.getLogger('release')


# Emoji shown per log level; blank messages (used for spacing) are left untouched.
LEVEL_EMOJI = {
	logging.DEBUG: '🐛',
	logging.INFO: 'ℹ️',
	logging.WARNING: '⚠️',
	logging.ERROR: '❌',
	logging.CRITICAL: '🔥',
}


class _EmojiFormatter(logging.Formatter):
	def format(self, record):
		message = record.getMessage()
		emoji = LEVEL_EMOJI.get(record.levelno, '')
		return f'{emoji} {message}' if message and emoji else message


def configure_logging():
	if logger.handlers:
		return
	handler = logging.StreamHandler(sys.stdout)
	handler.setFormatter(_EmojiFormatter())
	logger.addHandler(handler)
	logger.setLevel(logging.INFO)
	logger.propagate = False


def run(args, capture=False, check=True, mutating=False):
	"""Run a subprocess command from the repository root.

	When the command mutates state and dry-run is enabled, the command is only
	printed and not executed.
	"""
	if mutating and DRY_RUN:
		logger.info(f'[dry-run] {" ".join(args)}')
		return ''
	result = subprocess.run(
		args,
		cwd=ROOT,
		text=True,
		capture_output=True,
		check=False,
	)
	if not capture:
		sys.stdout.write(result.stdout)
		sys.stderr.write(result.stderr)
	if check and result.returncode != 0:
		output = (result.stderr or result.stdout or '').strip()
		raise ReleaseError(f'Command failed ({result.returncode}): {" ".join(args)}\n{output}')
	return result.stdout.strip() if capture else ''


def git(*args, capture=False, check=True, mutating=False):
	return run(['git', *args], capture=capture, check=check, mutating=mutating)


# Git inspection helpers.

def current_branch():
	return git('rev-parse', '--abbrev-ref', 'HEAD', capture=True)


def short_sha():
	return git('rev-parse', '--short', 'HEAD', capture=True)


def is_working_tree_clean():
	return git('status', '--porcelain', capture=True) == ''


def local_branch_exists(name):
	return git('rev-parse', '--verify', '--quiet', f'refs/heads/{name}', capture=True, check=False) != ''


def remote_branch_exists(name):
	return git('ls-remote', '--heads', REMOTE, name, capture=True) != ''


def local_tag_exists(name):
	return git('rev-parse', '--verify', '--quiet', f'refs/tags/{name}', capture=True, check=False) != ''


def remote_tag_exists(name):
	return git('ls-remote', '--tags', REMOTE, name, capture=True) != ''


def tag_exists(name):
	return local_tag_exists(name) or remote_tag_exists(name)


# Pre-flight guards.

def require_clean_tree():
	if not is_working_tree_clean():
		raise ReleaseError('Working tree is not clean; commit or stash your changes first.')


def require_on_branch(name):
	if current_branch() != name:
		raise ReleaseError(f'This procedure must be run from the "{name}" branch (currently on "{current_branch()}").')


def require_up_to_date(name):
	"""Ensure the local branch matches its remote counterpart exactly."""
	remote_ref = f'{REMOTE}/{name}'
	local = git('rev-parse', name, capture=True)
	remote = git('rev-parse', remote_ref, capture=True)
	if local == remote:
		return
	base = git('merge-base', name, remote_ref, capture=True)
	if remote == base:
		raise ReleaseError(f'"{name}" is ahead of "{remote_ref}"; push your commits first.')
	if local == base:
		raise ReleaseError(f'"{name}" is behind "{remote_ref}"; pull the latest changes first.')
	raise ReleaseError(f'"{name}" and "{remote_ref}" have diverged; reconcile them first.')


# Version-file bumping.

def bump_pom(version):
	content = POM.read_text()
	updated, count = re.subn(
		r'(<version>)[^<]*(</version>)',
		rf'\g<1>{version}\g<2>',
		content,
		count=1,
	)
	if count != 1:
		raise ReleaseError(f'Could not find a <version> element in {POM}.')
	if DRY_RUN:
		logger.info(f'[dry-run] set version in {POM.relative_to(ROOT)} to {version}')
		return
	POM.write_text(updated)


def bump_json_version(path, version):
	"""Rewrite every top-level `"version": "..."` occurrence in a JSON file.

	This targets the leading-whitespace-anchored version keys used by both
	package.json (one occurrence) and package-lock.json (root and the empty
	package entry), leaving nested dependency versions untouched.
	"""
	content = path.read_text()
	updated, count = re.subn(
		r'^(\s*"version":\s*")[^"]*(")',
		rf'\g<1>{version}\g<2>',
		content,
		flags=re.MULTILINE,
	)
	if count == 0:
		raise ReleaseError(f'Could not find a "version" field in {path}.')
	if DRY_RUN:
		logger.info(f'[dry-run] set version in {path.relative_to(ROOT)} to {version} ({count} occurrence(s))')
		return
	path.write_text(updated)


def bump_all_versions(version):
	bump_pom(version)
	for frontend in FRONTENDS:
		directory = ROOT / 'frontends' / frontend
		bump_json_version(directory / 'package.json', version)
		bump_json_version(directory / 'package-lock.json', version)


def bump_dev_placeholder_versions():
	"""Reset dev to its fixed placeholder versions (POM and frontends differ)."""
	bump_pom(DEV_POM_VERSION)
	for frontend in FRONTENDS:
		directory = ROOT / 'frontends' / frontend
		bump_json_version(directory / 'package.json', DEV_FRONTEND_VERSION)
		bump_json_version(directory / 'package-lock.json', DEV_FRONTEND_VERSION)


def changed_version_files():
	files = [str(POM.relative_to(ROOT))]
	for frontend in FRONTENDS:
		files.append(f'frontends/{frontend}/package.json')
		files.append(f'frontends/{frontend}/package-lock.json')
	return files


def commit_version_files(message):
	git('add', *changed_version_files(), mutating=True)
	if not DRY_RUN and git('diff', '--cached', '--name-only', capture=True) == '':
		logger.info('No version changes to commit; skipping commit.')
		return
	git('commit', '-m', message, mutating=True)


# RC number deduction.

def next_rc_number(version):
	"""Return the next release-candidate number for the given base version."""
	output = git('tag', '--list', f'v{version}-rc*', capture=True)
	numbers = []
	pattern = re.compile(rf'^v{re.escape(version)}-rc(\d+)$')
	for line in output.splitlines():
		match = pattern.match(line.strip())
		if match:
			numbers.append(int(match.group(1)))
	return max(numbers) + 1 if numbers else 1


# User interaction.

def confirm(assume_yes):
	if assume_yes or DRY_RUN:
		return True
	answer = input('Proceed? [y/N] ').strip().lower()
	return answer in ('y', 'yes')


def summary(title, lines):
	"""Print a structured report block; this is console output, not a log message."""
	print()
	print(f'== {title} ==')
	for line in lines:
		print(f'  {line}')
	print()


def recovery(lines):
	"""Print a structured report block; this is console output, not a log message."""
	print()
	print('The release did not complete. To recover, you may need to run:')
	for line in lines:
		print(f'  {line}')


# Procedures.

def procedure_branch(version, assume_yes):
	branch = release_branch(version)
	require_clean_tree()
	require_on_branch(DEV_BRANCH)
	git('fetch', REMOTE, DEV_BRANCH, '--tags', mutating=False)
	require_up_to_date(DEV_BRANCH)
	if local_branch_exists(branch) or remote_branch_exists(branch):
		raise ReleaseError(f'Branch "{branch}" already exists.')
	if tag_exists(f'v{version}'):
		raise ReleaseError(f'Version "{version}" was already released (tag "v{version}" exists).')

	snapshot = f'{version}-SNAPSHOT'
	summary('Create release branch', [
		f'{"Source branch":<16} : {DEV_BRANCH} ({short_sha()})',
		f'{"New branch":<16} : {branch}',
		f'{"Version":<16} : {snapshot}',
	])
	if not confirm(assume_yes):
		logger.info('Aborted.')
		return

	git('checkout', DEV_BRANCH, mutating=True)
	git('pull', '--ff-only', REMOTE, DEV_BRANCH, mutating=True)
	git('checkout', '-b', branch, mutating=True)
	bump_all_versions(snapshot)
	commit_version_files(f'Prepare release {version}')
	git('push', '-u', REMOTE, branch, mutating=True)
	logger.info(f'Release branch "{branch}" created and pushed.')


def procedure_rc(version, assume_yes):
	branch = release_branch(version)
	require_clean_tree()
	git('fetch', REMOTE, '--tags', mutating=False)
	if not local_branch_exists(branch) and not remote_branch_exists(branch):
		raise ReleaseError(f'Release branch "{branch}" does not exist; run "branch {version}" first.')

	number = next_rc_number(version)
	rc_version = f'{version}-rc.{number}'
	tag = f'v{version}-rc{number}'
	if tag_exists(tag):
		raise ReleaseError(f'Tag "{tag}" already exists.')

	summary('Create release candidate', [
		f'{"Release branch":<16} : {branch}',
		f'{"Version":<16} : {rc_version}',
		f'{"Tag":<16} : {tag} (triggers a CI prerelease and Docker image)',
	])
	if not confirm(assume_yes):
		logger.info('Aborted.')
		return

	git('checkout', branch, mutating=True)
	git('pull', '--ff-only', REMOTE, branch, mutating=True)
	require_up_to_date(branch)
	bump_all_versions(rc_version)
	commit_version_files(f'Release candidate {rc_version}')
	git('push', REMOTE, branch, mutating=True)
	try:
		git('tag', '-a', tag, '-m', f'Release candidate {rc_version}', mutating=True)
		git('push', REMOTE, tag, mutating=True)
	except ReleaseError:
		recovery([f'git tag -d {tag}'])
		raise
	logger.info(f'Release candidate "{tag}" created and pushed.')


def procedure_release(version, assume_yes):
	branch = release_branch(version)
	tag = f'v{version}'
	require_clean_tree()
	git('fetch', REMOTE, '--tags', mutating=False)
	if not local_branch_exists(branch) and not remote_branch_exists(branch):
		raise ReleaseError(f'Release branch "{branch}" does not exist; run "branch {version}" first.')
	if tag_exists(tag):
		raise ReleaseError(f'Tag "{tag}" already exists.')

	summary('Final release', [
		f'{"Release branch":<16} : {branch}',
		f'{"Version":<16} : {version}',
		f'{"Merge into":<16} : {MAIN_BRANCH}',
		f'{"Tag":<16} : {tag}',
		f'{"Back-merge":<16} : {MAIN_BRANCH} -> {DEV_BRANCH}, reset to {DEV_POM_VERSION} / {DEV_FRONTEND_VERSION}',
		f'{"Then delete":<16} : {branch}',
	])
	if not confirm(assume_yes):
		logger.info('Aborted.')
		return

	# Merge the release branch into main, bumping to the final version in the same commit.
	git('checkout', branch, mutating=True)
	git('pull', '--ff-only', REMOTE, branch, mutating=True)
	require_up_to_date(branch)
	git('checkout', MAIN_BRANCH, mutating=True)
	git('pull', '--ff-only', REMOTE, MAIN_BRANCH, mutating=True)
	require_up_to_date(MAIN_BRANCH)
	git('merge', '--no-ff', '--no-commit', branch, mutating=True)
	bump_all_versions(version)
	git('add', *changed_version_files(), mutating=True)
	git('commit', '-m', f'Release {version}', mutating=True)
	git('push', REMOTE, MAIN_BRANCH, mutating=True)
	try:
		git('tag', '-a', tag, '-m', f'Release {version}', mutating=True)
		git('push', REMOTE, tag, mutating=True)
	except ReleaseError:
		recovery([f'git tag -d {tag}'])
		raise

	# Back-merge main into dev, folding the placeholder reset into the same commit.
	git('checkout', DEV_BRANCH, mutating=True)
	git('pull', '--ff-only', REMOTE, DEV_BRANCH, mutating=True)
	git('merge', '--no-ff', '--no-commit', MAIN_BRANCH, mutating=True)
	# The merge just brought in main's released (non-SNAPSHOT) version files;
	# dev never carries a semantic version, so reset it to the placeholders before committing.
	bump_dev_placeholder_versions()
	git('add', *changed_version_files(), mutating=True)
	git('commit', '-m', f'Back-merge release {version} into {DEV_BRANCH}', mutating=True)
	git('push', REMOTE, DEV_BRANCH, mutating=True)

	# Delete the release branch.
	git('branch', '-d', branch, mutating=True)
	if remote_branch_exists(branch):
		git('push', REMOTE, '--delete', branch, mutating=True)
	logger.info(f'Release "{tag}" published; "{branch}" deleted and "{DEV_BRANCH}" synced.')


def procedure_cleanup(version, assume_yes):
	branch = release_branch(version)
	git('fetch', REMOTE, '--tags', mutating=False)
	rc_tags = [
		line.strip()
		for line in git('tag', '--list', f'v{version}-rc*', capture=True).splitlines()
		if line.strip()
	]
	branch_present = local_branch_exists(branch) or remote_branch_exists(branch)
	if not branch_present and not rc_tags:
		logger.info(f'Nothing to clean up for {version}.')
		return

	summary('Cleanup', [
		f'{"Release branch":<16} : {branch if branch_present else "(none)"}',
		f'{"RC tags":<16} : {", ".join(rc_tags) if rc_tags else "(none)"}',
	])
	if not confirm(assume_yes):
		logger.info('Aborted.')
		return

	if branch_present:
		if local_branch_exists(branch):
			git('branch', '-D', branch, mutating=True)
		if remote_branch_exists(branch):
			git('push', REMOTE, '--delete', branch, mutating=True)
	for tag in rc_tags:
		if local_tag_exists(tag):
			git('tag', '-d', tag, mutating=True)
		if remote_tag_exists(tag):
			git('push', REMOTE, '--delete', tag, mutating=True)
	logger.info(f'Cleanup for {version} complete.')


PROCEDURES = {
	'branch': procedure_branch,
	'rc': procedure_rc,
	'release': procedure_release,
	'cleanup': procedure_cleanup,
}


def build_parser():
	parser = argparse.ArgumentParser(description='Manage Rodano releases using the Git-flow process.')
	parser.add_argument('--dry-run', action='store_true', help='Run all checks and print commands without mutating anything.')
	parser.add_argument('--yes', action='store_true', help='Skip the interactive confirmation prompt.')
	subparsers = parser.add_subparsers(dest='procedure', required=True)
	for name in PROCEDURES:
		sub = subparsers.add_parser(name, help=f'{name} procedure')
		sub.add_argument('version', help='Target release version as X.Y.Z (without the "v" prefix).')
	return parser


def main(argv=None):
	global DRY_RUN
	configure_logging()
	parser = build_parser()
	args = parser.parse_args(argv)
	DRY_RUN = args.dry_run

	if not VERSION_RE.match(args.version):
		parser.error(f'Invalid version "{args.version}"; expected X.Y.Z without a "v" prefix.')

	try:
		PROCEDURES[args.procedure](args.version, args.yes)
	except ReleaseError as error:
		logger.error(str(error))
		return 1
	return 0


if __name__ == '__main__':
	sys.exit(main())
