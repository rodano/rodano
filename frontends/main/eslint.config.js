import js from '@eslint/js';
import stylistic from '@stylistic/eslint-plugin';
import tseslint from 'typescript-eslint';
import angular from 'angular-eslint';
import globals from 'globals';

export default tseslint.config(
	{
		files: ['**/*.ts'],
		ignores: [
			'src/app/core/model/*'
		],
		extends: [
			js.configs.recommended,
			stylistic.configs['recommended-flat'],
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
			'strict': 'error', //suggestion
			'yoda': 'error', //suggestion
			//stylistic rules
			...stylistic.configs['recommended-flat'].rules,
			'@stylistic/arrow-parens': ['error', 'as-needed'],
			'@stylistic/block-spacing': ["error", "never"],
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
			'no-unused-vars': 'off', //disable the base rule because it conflicts TypeScript rules
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
					style: 'camelCase',
				},
			],
			'@angular-eslint/component-class-suffix': [
				'error',
				{
					suffixes: ['Component', 'Dialog']
				}
			],
			'@angular-eslint/component-selector': [
				'error',
				{
					type: 'element',
					prefix: 'app',
					style: 'kebab-case',
				},
			]
		}
	},
	{
		files: ['**/*.html'],
		extends: [
			...angular.configs.templateRecommended,
		],
		rules: {
			'@angular-eslint/template/alt-text': 'error',
			'@angular-eslint/template/cyclomatic-complexity': 'error',
			'@angular-eslint/template/no-duplicate-attributes': 'error',
			'@angular-eslint/template/no-interpolation-in-attributes': 'error',
			'@angular-eslint/template/prefer-control-flow': 'error',
			'@angular-eslint/template/use-track-by-function': 'error'
		}
	}
);
