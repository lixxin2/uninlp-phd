package edu.hitsz.nlp.pinyin;

public class PyConstants {
	
	/** 声母,21 */
	public static String[] initials = {
		"b","p","m","f","d","t","n","l",
		"g","k","h","j","q","x",
		"zh","ch","sh","r","z","c","s"
		//"y","w","yu" 分别为有韵头的零声母音节，韵头i、u、ü的改写，不应划入声母。
		};
	/** 韵母,35,37("ê","er") */
	public static String[] finals = {
			"i","u","ü",
		"ɑ","iɑ","uɑ",
		"o",	"uo",
		"e",
		"ê","ie",	"üe",
		"er",
		"ɑi",	"uɑi",
		"ei",	"uei",
		"ɑo","iɑo",
		"ou","iou",
		"ɑn","iɑn","uɑn","üɑn",
		"en","in","uen","ün",
		"ɑnɡ","iɑnɡ","uɑnɡ",
		"enɡ","inɡ","uenɡ",
					"onɡ","ionɡ" 
		};
	/** 声调 */
	public static String[] tones = {"1","2","3","4","0"};
	/** 无调拼音 */
	public static String[] yins = {};

}
