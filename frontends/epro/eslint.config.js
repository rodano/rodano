import {defineConfig} from 'eslint/config';
import js from '@eslint/js';
import stylistic from '@stylistic/eslint-plugin';
import tseslint from 'typescript-eslint';
import angular from 'angular-eslint';
import globals from 'globals';

export default defineConfig(
	{
		files: ['**/*.ts'],
		extends: [
			js.configs.recommended,
			stylistic.configs['recommended'],
			...tseslint.configs.recommended,
			...tseslint.configs.stylistic,
			...angular.configs.tsRecommended
		],
		plugins: {
			'@stylistic': stylistic
		},
		processor: angular.processInlineTemplates,
		languageOptions: {
			globals: {
				...globals.browser,
				...globals.jasmine
			}
		},
		linterOptions: {
			reportUnusedDisableDirectives: 'error'
		},
		rules: {
			//base rules
			...js.configs.recommended.rules,
			'array-callback-return': 'error',
			'consistent-return': 'error',
			'curly': 'error',
			'default-case-last': 'error',
			'default-param-last': 'error',
			'eqeqeq': 'error',
			'no-array-constructor': 'error',
			'no-await-in-loop': 'error',
			'no-console': 'off',
			'no-constructor-return': 'error',
			'no-duplicate-imports': 'error',
			'no-inner-declarations': 'error',
			'no-new-wrappers': 'error',
			'no-promise-executor-return': 'error',
			'no-proto': 'error',
			'no-prototype-builtins': 'off',
			'no-unmodified-loop-condition': 'error',
			'no-unreachable-loop': 'error',
			'no-use-before-define': 'error',
			'no-useless-assignment': 'error',
			'no-self-compare': 'error',
			'no-template-curly-in-string': 'error',
			'no-var': 'error',
			'no-with': 'error',
			'prefer-const': 'error',
			'prefer-template': 'error',
			'require-atomic-updates': 'error',
			'strict': 'error',
			'yoda': 'error',
			//stylistic rules
			...stylistic.configs['recommended'].rules,
			'@stylistic/arrow-parens': ['error', 'as-needed'],
			'@stylistic/block-spacing': ['error', 'never'],
			'@stylistic/comma-dangle': ['error', 'never'],
			'@stylistic/eol-last': ['error', 'always'],
			'@stylistic/indent': ['error', 'tab', {SwitchCase: 1}],
			'@stylistic/key-spacing': 'error',
			'@stylistic/keyword-spacing': ['error', {'overrides': {'if': {after: false}, 'for': {after: false}, 'switch': {after: false}, 'while': {after: false}}}],
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
			'@stylistic/spaced-comment': ['error', 'never'],
			//additions to typescript-eslint
			'no-unused-vars': 'off',
			...tseslint.configs.recommendedTypeChecked[0].rules,
			...tseslint.configs.stylisticTypeChecked[0].rules,
			'@typescript-eslint/dot-notation': 'off',
			'@typescript-eslint/no-explicit-any': 'off',
			'@typescript-eslint/no-unused-vars': ['error'],
			'@typescript-eslint/prefer-nullish-coalescing': 'off',
			'@typescript-eslint/unbound-method': 'off',
			//additions to angular-eslint
			...angular.configs.tsRecommended[1].rules,
			'@angular-eslint/directive-selector': [
				'error',
				{
					type: 'attribute',
					prefix: 'app',
					style: 'camelCase'
				}
			],
			'@angular-eslint/component-selector': [
				'error',
				{
					type: 'element',
					prefix: 'app',
					style: 'kebab-case'
				}
			],
			//constructor injection is still valid; migrating to inject() is a future refactoring task
			'@angular-eslint/prefer-inject': 'off'
		}
	},
	{
		files: ['**/*.spec.ts'],
		languageOptions: {
			globals: {
				...globals.browser,
				...globals.vitest
			}
		}
	},
	{
		files: ['**/*.html'],
		extends: [
			...angular.configs.templateRecommended,
			...angular.configs.templateAccessibility
		],
		rules: {}
	}
);
