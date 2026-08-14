import js from '@eslint/js';
import stylistic from '@stylistic/eslint-plugin';
import html from '@html-eslint/eslint-plugin';
import htmlparser from '@html-eslint/parser';
import globals from 'globals';

export default [
	{
		languageOptions: {
			globals: {
				...globals.nodeBuiltin,
				...globals.browser
			}
		},
		plugins: {
			'@stylistic': stylistic
		},
		linterOptions: {
			reportUnusedDisableDirectives: 'error'
		},
		//include recommended rules directly in the rules object
		//it's better to expand the recommended rules here instead of adding them as one of the configuration in the root configurations array
		//that's because if other configuration options are specified (especially included and excluded files), they will be applied globally
		rules: {
			//base rules
			...js.configs.recommended.rules,
			'array-callback-return': 'error', //possible problem
			'consistent-return': 'error', //suggestion
			'curly': 'error', //suggestion
			'default-case-last': 'error', //suggestion
			'default-param-last': 'error', //suggestion
			'eqeqeq': 'error', //suggestion
			'no-array-constructor': 'error', //suggestion
			'no-await-in-loop': 'error', //possible problem
			'no-console': 'off', //suggestion
			'no-constructor-return': 'error', //possible problem
			'no-duplicate-imports': 'error', //possible problem
			'no-inner-declarations': 'error', //possible problem
			'no-new-wrappers': 'error', //suggestion
			'no-promise-executor-return': 'error', //possible problem
			'no-proto': 'error', //suggestion
			'no-prototype-builtins': 'off', //default possible problem, but disabled
			'no-unmodified-loop-condition': 'error', //possible problem
			'no-unreachable-loop': 'error', //possible problem
			'no-use-before-define': 'error', //possible problem
			'no-useless-assignment': 'error', //possible problem
			'no-self-compare': 'error', //possible problem
			'no-template-curly-in-string': 'error', //possible problem
			'no-var': 'error', //suggestion
			'no-with': 'error', //suggestion
			'prefer-const': 'error', //suggestion
			'prefer-template': 'error', //suggestion
			'require-atomic-updates': 'error', //possible problem
			'require-await': 'error', //suggestion
			'strict': 'error', //suggestion
			'yoda': 'error', //suggestion
			//stylistic rules
			...stylistic.configs['recommended'].rules,
			'@stylistic/arrow-parens': ['error', 'as-needed'],
			'@stylistic/comma-dangle': ['error', 'never'],
			'@stylistic/eol-last': ['error', 'always'],
			'@stylistic/indent': ['error', 'tab', {SwitchCase: 1}],
			'@stylistic/key-spacing': 'error',
			'@stylistic/keyword-spacing': ['error', {overrides: {if: {after: false}, for: {after: false}, switch: {after: false}, while: {after: false}}}],
			'@stylistic/linebreak-style': 'error',
			'@stylistic/member-delimiter-style': ['error', {multiline: {delimiter: 'semi'}}],
			'@stylistic/no-extra-semi': 'error',
			'@stylistic/no-multi-spaces': 'error',
			'@stylistic/no-multiple-empty-lines': 'error',
			'@stylistic/no-tabs': ['error', {allowIndentationTabs: true}],
			'@stylistic/no-trailing-spaces': 'error',
			'@stylistic/object-curly-spacing': ['error', 'never'],
			'@stylistic/quotes': ['error', 'single'],
			'@stylistic/semi': ['error', 'always', {omitLastInOneLineBlock: false}],
			'@stylistic/space-before-function-paren': ['error', 'never'],
			'@stylistic/spaced-comment': ['error', 'never']
		}
	},
	{
		files: ['**/*.html'],
		languageOptions: {
			parser: htmlparser
		},
		plugins: {
			'@html-eslint': html
		},
		rules: {
			...html.configs['flat/recommended'].rules,
			'@html-eslint/attrs-newline': ['error', {ifAttrsMoreThan: 4}],
			'@html-eslint/element-newline': ['error', {skip: ['a', 'code', 'p', 'pre', 'span', 'strong', 'time'], inline: ['$inline']}],
			'@html-eslint/id-naming-convention': ['error', 'kebab-case'],
			'@html-eslint/indent': ['error', 'tab'],
			'@html-eslint/lowercase': 'error',
			'@html-eslint/no-extra-spacing-attrs': 'error',
			'@html-eslint/no-multiple-empty-lines': 'error',
			'@html-eslint/no-non-scalable-viewport': 'error',
			'@html-eslint/no-skip-heading-levels': 'error',
			'@html-eslint/no-target-blank': 'error',
			'@html-eslint/no-trailing-spaces': 'error',
			'@html-eslint/quotes': 'error',
			'@html-eslint/require-closing-tags': ['error', {selfClosing: 'always'}],
			'@html-eslint/require-meta-charset': 'error',
			'@html-eslint/require-meta-description': 'error'
		}
	}
];
