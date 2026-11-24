import {defineConfig, globalIgnores} from 'eslint/config';
import js from '@eslint/js';
import stylistic from '@stylistic/eslint-plugin';
import globals from 'globals';

export default defineConfig([
	globalIgnores(['dist']),
	{
		plugins: {
			'@stylistic': stylistic
		},
		languageOptions: {
			globals: {
				...globals.node
			}
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
	}
]);
