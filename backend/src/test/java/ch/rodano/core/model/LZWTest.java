package ch.rodano.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.core.model.configuration.LZW;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LZWTest {

	@Test
	@DisplayName("LZWTest")
	public void test() {
		final String string = "aaa";
		assertAll("Compress/Decompress",
			() -> assertEquals("aĀ", LZW.compressToString("aaa")),
			() -> assertEquals("aaa", LZW.decompressString("aĀ")),
			() -> assertEquals(string, LZW.decompressString(LZW.compressToString(string))),

			() -> assertEquals("taraĀĀ", LZW.compressToString("taratata")),
			() -> assertEquals("taratata", LZW.decompressString("taraĀĀ")),

			() -> assertEquals("some text will no encode", LZW.compressToString("some text will no encode")),
			() -> assertEquals("some text will no encode", LZW.decompressString("some text will no encode")));

		final String text = "TOBEORNOTTOBEORTOBEORNOT";
		final String encodedText = "TOBEORNOTĀĂĄĉăąć";
		assertEquals(encodedText, LZW.compressToString(text));
		assertEquals(text, LZW.decompressString(LZW.compressToString(text)));
		assertEquals(text, LZW.decompressString(encodedText));

		assertEquals("a	text	with	someāabulations", LZW.compressToString("a	text	with	some	tabulations"));
		assertEquals("a	text	with	some	tabulations", LZW.decompressString("a	text	with	someāabulations"));

		final String tabbedText = "			one				two					three";
		assertEquals(tabbedText, LZW.decompressString(LZW.compressToString(tabbedText)));

		assertEquals(
			"{\"id\": \"Test\",\"shortname\": {\"en\": \"Test\",},\"longname\": {\"en\": \"Test\",},\"description\": {},\"url\": \"http://127.0.0.1:8080\",\"email\": \"info@rodanotech.ch\",\"emailCheckMode\": \"NONE\",\"passwordStrong\": false,\"passwordLength\": 4,\"passwordValidityDuration\": 0,\"passwordMaxAttemptsPerDay\": 0,\"passwordUniqueness\": false,\"introductionText\": \"Rodano Test Study\",\"copyright\": \"Copyright &#169; 2008-2011 RODANOTECH, All rights reserved.\",\"configVersion\": 5,\"configDate\": 1348752519734,\"configUser\": \"mcorag\",\"languageIds\": [\"en\",\"fr\"],\"defaultLanguageId\": \"en\",\"userAttributes\": [\"PHONE\"],\"countries\": {\"FR\": {\"id\": \"FR\",\"shortname\": {\"en\": \"France\"},\"longname\": {},\"description\": {},\"entity\": \"COUNTRY\"},\"GB\": {\"id\": \"GB\",\"shortname\": {\"en\": \"United Kindgow\"},\"longname\": {},\"description\": {},\"entity\": \"COUNTRY\"}},\"languages\": {},\"scopeModels\": {\"Country\": {\"id\": \"Country\",\"shortname\": {\"en\": \"Country\",},\"pluralShortname\": {\"en\": \"Countries\",},\"longname\": {\"en\": \"Country\",},\"description\": {},\"defaultParentId\": \"Study\",\"parents\": [\"Study\"],\"virtual\": false,\"scopeFormat\": \"${parent}-${siblingsNumber:2}\",\"lastUpdateTime\": \"2009-04-28T19:38:35.000+0000\",\"workflows\": [],\"crfMode\": \"NORMAL\",\"color\": \"#72bf44\",\"searchComplements\": false,\"entity\": \"SCOPE_MODEL\"},\"Patient\": {\"id\": \"Patient\",\"shortname\": {\"en\": \"Patient\",},\"pluralShortname\": {\"en\": \"Patients\",},\"longname\": {\"en\": \"Patient\",},\"description\": {},\"defaultParentId\": \"Center\",\"parents\": [\"Center\"],\"virtual\": false,\"maxNumber\": 600,\"scopeFormat\": \"${parent}-${siblingsNumber:2}\",\"lastUpdateTime\": \"2009-04-29T11:16:40.000+0000\",\"workflows\": [],\"crfMode\": \"NORMAL\",\"scopesPerPage\": 200,\"searchComplements\": false,\"entity\": \"SCOPE_MODEL\"}}}",
			LZW.decompressString("{\"id\": \"Test\",\"shortnameĄ ĀenĘćĉċ,}člongĔĖĘĚĜąĞĊČĢ\"dĉcriptiĥĪıurlĝhttp://127.0Ō.1:80ŒČ\"emaiŀĭinfo@rodanotech.ŪŕŗřlChũkMţėĭNONEŕpasswđdStŢĦĘfalsečſƁƃrdLěgthĘ4ƐƀƂƄVƌĂityDľaĹĻą0ƝƒƄMaxAŃŗĸsPerDayĘƭ\"ƑƟƔUniquěĉsƊƌƎčŝƇţucƪnĈxċĭRţťo ĈĊ ƆudƾčcopyĶgłĝCǪǬiǮt &#169; 20œ-ǿ11 RODAźTECH, All ǭłsȕĉƺved.ŕǩŞǴVƺsĺĬ 5ǨĥfǴƼŨĘ13487525197ȳȫȢgUƎrĝmǩragŕlťguɈeIdǌą[ŖĜčfɃ]čĳƋultLɌɎgɐăĭěŕuɂƴƇibuŨɓ ɕPHŻŽɛ\"ǩunɮĉĪ\"FRʁĂĝʃŕďđēĕŸęɖʇɇncėıĤĦĨʎ{ıĳsĵķǖļčěĹƥǰOUNTRY\"ıGBʅɧĆʰʉĐĒʙʁɩĭǅƤȝ KŝdgowʮģĥħʍʢĲĴĶĸȧˌʤƤƾĭCʨʪʬʮʖɣɏɳʛčʞǪeŶĳƍʁǱɽƇ˕ʏʆ˖o˪rǧĎʷʌĩąīǰ˰ɾ˲İƐlƨlS˵ʹ˸ʐ˯˱iʀġˈʘˋ̆ʻĆ˩˼ǧʜˎʠˑ˸̖ɞɠParʤɑĝǤǦž̟ʤɳɕ̤ƾɺviĒɎś ƋƍƏĎǩpeFđŘǚĆ${ſ̠ɾ}-̀ȦblŝgsNumbƺ:2}ɊƀtUpŤŨTi˷Ćǿ09-04Ȃ8TȺ:38ͫ5ŌȀ+ȀȀŕƓkfĤw̩ɺĵf˥ʎ\"źRMALȠoĤɃĭ#72bf44ʉe̟ŪǱmplŗ̨Ǎ̵ʣɾ˔̣˗PE_MȈEΆı̞Ĺʤʲĝά̊ɾʶʋ̅ʏ̑\"αή̌ǁ̀ɇ̂̄̏ηȨιƩβtǌμʗˊ͟˹ĭκγμʝʟːƫę̛aɟt̞̓t̢˖ʤƺ̦ϝ̩\"Cϡɚč̮̰ƌΝǏ\"Řx͎͐Ϣą6Ȁˡ̸̺̼Ʃĝ̀͂ʤ͇ͅɯ͊Ħ͍͏͑r͓͕ģ͙͗͛e͟͝\"ͣͥ͡Ȃ9ͩŐǺ:4ōͳͲͳ͵đͷ\u0379ͻǨr;ŷĝ\u0382΄ΆϹˣƸƺ̞ɥĘ͡ˡΔrΖoΘΚĖɾɳ̴ϯ˓ʦĭSΣΥΧDΩ˛}"));
	}
}
