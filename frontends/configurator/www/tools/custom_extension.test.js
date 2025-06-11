import './custom_extension.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('String#idify', async feature => {
		await feature.it('works properly', () => {
			assert.equal('abcde'.idify(), 'ABCDE', 'Idify "abcde" gives "ABCDE"');
			assert.equal('AbC dE 12   3'.idify(), 'ABC_DE_12_3', 'Idify "AbC dE 12   3" gives "ABC_DE_12_3"');
			assert.equal('abçdé'.idify(), 'ABD', 'Idify removes unknown characters so "abçdé" gives "ABD"');
			assert.equal('#ab()çd\\é'.idify(), 'ABD', 'Idify removes unknown characters so "#ab()çd\\é" gives "ABD"');
			assert.equal('a_B/C.dE'.idify(), 'A_B_C_DE', 'Idify considers underscores, slashes and dots as separators so "a_B/C.dE" gives "A_B_C_DE"');
			assert.equal('aB__c  déêèe'.idify(), 'AB_C_DE', 'Idify reduces underscores so "aB__c  déêèe" gives "AB_C_DE"');
			assert.equal(''.idify(), '', 'Idify "" gives ""');
			const string = 'abçdé';
			const string_idified = string.idify();
			assert.equal(string, 'abçdé', 'Idify creates a new string');
			assert.notEqual(string, string_idified, 'Idify creates a new string');
		});
	});

	bundle.end();
}
