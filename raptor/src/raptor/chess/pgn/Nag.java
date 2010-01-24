/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess.pgn;

/**
 * Numeric Annotation Glyphs (NAG)
 * http://www.markalowery.net/Chess/Notation/NAG.html (From his site).
 * 
 * In some PGN files, especially older ones created before the use of Informant
 * symbols became universally common, you occasionally see odd annotations such
 * as $1, $2, $4, and so on. These are NAG's - Numeric Annotation Glyphs. Their
 * purpose is to indicate a common annotation idea with a small symbol instead
 * of a long text explanation. Just as "??" is quicker and easier to write than
 * "Horrible blunder that throws the game away", NAG's serve the same purpose.
 * 
 * The list below will explain the meaning of each NAG symbol and help you
 * understand what the annotator is indicating when you encounter these symbols.
 * For a complete list & definition of the more common Informant annotation
 * symbols, see The Symbols tutorial.
 * 
 * NAG zero is used for a null annotation; it is provided for the convenience of
 * software designers as a placeholder value and is usually not shown in game
 * annotations.
 * 
 * NAG's with values from 1 to 9 annotate the move just played and are
 * equivalent to the more familiar, "!" "?", "!?", etc.
 * 
 * NAG's with values from 10 to 135 are comments about the current position.
 * 
 * NAG's with values from 136 to 139 describe time pressure.
 * 
 * Other NAG values are reserved for future definition.
 * 
 * Note: the number assignments listed below are not etched in stone. It's quite
 * possible, and perhaps even likely, that they will be changed and redefined in
 * the future.
 * 
 * In PGN game scores, each number below would be prefaced with a $ symbol.
 * 
 * The class is an enum so it is forced into the Flyweight pattern and also
 * descriptions are not provided in this enum because they are
 * internationalizable. Currently they reside in the NAGMessages class.
 */
public enum Nag implements MoveAnnotation {

	// NAG_0("$0"),
	NAG_1("$1", "!"), NAG_2("$2", "?"), NAG_3("$3", "!!"), NAG_4("$4", "!?"), NAG_5(
			"$5", "?!"), NAG_6("$6"), NAG_7("$7"), NAG_8("$8"), NAG_9("$9"), NAG_10(
			"$10"), NAG_11("$11", "="), NAG_12("$12"), NAG_13("$13"), NAG_14(
			"$14", "+="), NAG_15("$15", "=+"), NAG_16("$16", "+/-"), NAG_17(
			"$17", "-/+"), NAG_18("$18", "+-"), NAG_19("$19", "-+"), NAG_20(
			"$20", "+--"), NAG_21("$21", "--+"), NAG_22("$22"), NAG_23("$23"), NAG_24(
			"$24"), NAG_25("$25"), NAG_26("$26"), NAG_27("$27"), NAG_28("$28"), NAG_29(
			"$29"), NAG_30("$30"), NAG_31("$31"), NAG_32("$32"), NAG_33("$33"), NAG_34(
			"$34"), NAG_35("$35"), NAG_36("$36"), NAG_37("$37"), NAG_38("$38"), NAG_39(
			"$39"), NAG_40("$40"), NAG_41("$41"), NAG_42("$42"), NAG_43("$43"), NAG_44(
			"$44"), NAG_45("$45"), NAG_46("$46"), NAG_47("$47"), NAG_48("$48"), NAG_49(
			"$49"), NAG_50("$50"), NAG_51("$51"), NAG_52("$52"), NAG_53("$53"), NAG_54(
			"$54"), NAG_55("$55"), NAG_56("$56"), NAG_57("$57"), NAG_58("$58"), NAG_59(
			"$59"), NAG_60("$60"), NAG_61("$61"), NAG_62("$62"), NAG_63("$63"), NAG_64(
			"$64"), NAG_65("$65"), NAG_66("$66"), NAG_67("$67"), NAG_68("$68"), NAG_69(
			"$69"), NAG_70("$70"), NAG_71("$71"), NAG_72("$72"), NAG_73("$73"), NAG_74(
			"$74"), NAG_75("$75"), NAG_76("$76"), NAG_77("$77"), NAG_78("$78"), NAG_79(
			"$79"), NAG_80("$80"), NAG_81("$81"), NAG_82("$82"), NAG_83("$83"), NAG_84(
			"$84"), NAG_85("$85"), NAG_86("$86"), NAG_87("$87"), NAG_88("$88"), NAG_89(
			"$89"), NAG_90("$90"), NAG_91("$91"), NAG_92("$92"), NAG_93("$93"), NAG_94(
			"$94"), NAG_95("$95"), NAG_96("$96"), NAG_97("$97"), NAG_98("$98"), NAG_99(
			"$99"), NAG_100("$100"), NAG_101("$101"), NAG_102("$102"), NAG_103(
			"$103"), NAG_104("$104"), NAG_105("$105"), NAG_106("$106"), NAG_107(
			"$107"), NAG_108("$108"), NAG_109("$109"), NAG_110("$110"), NAG_111(
			"$111"), NAG_112("$112"), NAG_113("$113"), NAG_114("$114"), NAG_115(
			"$115"), NAG_116("$116"), NAG_117("$117"), NAG_118("$118"), NAG_119(
			"$119"), NAG_120("$120"), NAG_121("$121"), NAG_122("$122"), NAG_123(
			"$123"), NAG_124("$124"), NAG_125("$125"), NAG_126("$126"), NAG_127(
			"$127"), NAG_128("$128"), NAG_129("$129"), NAG_130("$130"), NAG_131(
			"$131"), NAG_132("$132"), NAG_133("$133"), NAG_134("$134"), NAG_135(
			"$135"), NAG_136("$136"), NAG_137("$137"), NAG_138("$138"), NAG_139(
			"$139");

	public static Nag get(String nagString) {
		// if (nagString.equals("$0")) {
		// return NAG_0;
		// } else
		if (nagString.equals("$1")) {
			return NAG_1;
		} else if (nagString.equals("$2")) {
			return NAG_2;
		} else if (nagString.equals("$3")) {
			return NAG_3;
		} else if (nagString.equals("$4")) {
			return NAG_4;
		} else if (nagString.equals("$5")) {
			return NAG_5;
		} else if (nagString.equals("$6")) {
			return NAG_6;
		} else if (nagString.equals("$7")) {
			return NAG_7;
		} else if (nagString.equals("$8")) {
			return NAG_8;
		} else if (nagString.equals("$9")) {
			return NAG_9;
		} else if (nagString.equals("$10")) {
			return NAG_10;
		} else if (nagString.equals("$11")) {
			return NAG_11;
		} else if (nagString.equals("$12")) {
			return NAG_12;
		} else if (nagString.equals("$13")) {
			return NAG_13;
		} else if (nagString.equals("$14")) {
			return NAG_14;
		} else if (nagString.equals("$15")) {
			return NAG_15;
		} else if (nagString.equals("$16")) {
			return NAG_16;
		} else if (nagString.equals("$17")) {
			return NAG_17;
		} else if (nagString.equals("$18")) {
			return NAG_18;
		} else if (nagString.equals("$19")) {
			return NAG_19;
		} else if (nagString.equals("$20")) {
			return NAG_20;
		} else if (nagString.equals("$21")) {
			return NAG_21;
		} else if (nagString.equals("$22")) {
			return NAG_22;
		} else if (nagString.equals("$23")) {
			return NAG_23;
		} else if (nagString.equals("$24")) {
			return NAG_24;
		} else if (nagString.equals("$25")) {
			return NAG_25;
		} else if (nagString.equals("$26")) {
			return NAG_26;
		} else if (nagString.equals("$27")) {
			return NAG_27;
		} else if (nagString.equals("$28")) {
			return NAG_28;
		} else if (nagString.equals("$29")) {
			return NAG_29;
		} else if (nagString.equals("$30")) {
			return NAG_30;
		} else if (nagString.equals("$31")) {
			return NAG_31;
		} else if (nagString.equals("$32")) {
			return NAG_32;
		} else if (nagString.equals("$33")) {
			return NAG_33;
		} else if (nagString.equals("$34")) {
			return NAG_34;
		} else if (nagString.equals("$35")) {
			return NAG_35;
		} else if (nagString.equals("$36")) {
			return NAG_36;
		} else if (nagString.equals("$37")) {
			return NAG_37;
		} else if (nagString.equals("$38")) {
			return NAG_38;
		} else if (nagString.equals("$39")) {
			return NAG_39;
		} else if (nagString.equals("$40")) {
			return NAG_40;
		} else if (nagString.equals("$41")) {
			return NAG_41;
		} else if (nagString.equals("$42")) {
			return NAG_42;
		} else if (nagString.equals("$43")) {
			return NAG_43;
		} else if (nagString.equals("$44")) {
			return NAG_44;
		} else if (nagString.equals("$45")) {
			return NAG_45;
		} else if (nagString.equals("$46")) {
			return NAG_46;
		} else if (nagString.equals("$47")) {
			return NAG_47;
		} else if (nagString.equals("$48")) {
			return NAG_48;
		} else if (nagString.equals("$49")) {
			return NAG_49;
		} else if (nagString.equals("$50")) {
			return NAG_50;
		} else if (nagString.equals("$51")) {
			return NAG_51;
		} else if (nagString.equals("$52")) {
			return NAG_52;
		} else if (nagString.equals("$53")) {
			return NAG_53;
		} else if (nagString.equals("$54")) {
			return NAG_54;
		} else if (nagString.equals("$55")) {
			return NAG_55;
		} else if (nagString.equals("$56")) {
			return NAG_56;
		} else if (nagString.equals("$57")) {
			return NAG_57;
		} else if (nagString.equals("$58")) {
			return NAG_58;
		} else if (nagString.equals("$59")) {
			return NAG_59;
		} else if (nagString.equals("$60")) {
			return NAG_60;
		} else if (nagString.equals("$61")) {
			return NAG_61;
		} else if (nagString.equals("$62")) {
			return NAG_62;
		} else if (nagString.equals("$63")) {
			return NAG_63;
		} else if (nagString.equals("$64")) {
			return NAG_64;
		} else if (nagString.equals("$65")) {
			return NAG_65;
		} else if (nagString.equals("$66")) {
			return NAG_66;
		} else if (nagString.equals("$67")) {
			return NAG_67;
		} else if (nagString.equals("$68")) {
			return NAG_68;
		} else if (nagString.equals("$69")) {
			return NAG_69;
		} else if (nagString.equals("$70")) {
			return NAG_70;
		} else if (nagString.equals("$71")) {
			return NAG_71;
		} else if (nagString.equals("$72")) {
			return NAG_72;
		} else if (nagString.equals("$73")) {
			return NAG_73;
		} else if (nagString.equals("$74")) {
			return NAG_74;
		} else if (nagString.equals("$75")) {
			return NAG_75;
		} else if (nagString.equals("$76")) {
			return NAG_76;
		} else if (nagString.equals("$77")) {
			return NAG_77;
		} else if (nagString.equals("$78")) {
			return NAG_78;
		} else if (nagString.equals("$79")) {
			return NAG_79;
		} else if (nagString.equals("$80")) {
			return NAG_80;
		} else if (nagString.equals("$81")) {
			return NAG_81;
		} else if (nagString.equals("$82")) {
			return NAG_82;
		} else if (nagString.equals("$83")) {
			return NAG_83;
		} else if (nagString.equals("$84")) {
			return NAG_84;
		} else if (nagString.equals("$85")) {
			return NAG_85;
		} else if (nagString.equals("$86")) {
			return NAG_86;
		} else if (nagString.equals("$87")) {
			return NAG_87;
		} else if (nagString.equals("$88")) {
			return NAG_88;
		} else if (nagString.equals("$89")) {
			return NAG_89;
		} else if (nagString.equals("$90")) {
			return NAG_90;
		} else if (nagString.equals("$91")) {
			return NAG_91;
		} else if (nagString.equals("$92")) {
			return NAG_92;
		} else if (nagString.equals("$93")) {
			return NAG_93;
		} else if (nagString.equals("$94")) {
			return NAG_94;
		} else if (nagString.equals("$95")) {
			return NAG_95;
		} else if (nagString.equals("$96")) {
			return NAG_96;
		} else if (nagString.equals("$97")) {
			return NAG_97;
		} else if (nagString.equals("$98")) {
			return NAG_98;
		} else if (nagString.equals("$99")) {
			return NAG_99;
		} else if (nagString.equals("$100")) {
			return NAG_100;
		} else if (nagString.equals("$101")) {
			return NAG_101;
		} else if (nagString.equals("$102")) {
			return NAG_102;
		} else if (nagString.equals("$103")) {
			return NAG_103;
		} else if (nagString.equals("$104")) {
			return NAG_104;
		} else if (nagString.equals("$105")) {
			return NAG_105;
		} else if (nagString.equals("$106")) {
			return NAG_106;
		} else if (nagString.equals("$107")) {
			return NAG_107;
		} else if (nagString.equals("$108")) {
			return NAG_108;
		} else if (nagString.equals("$109")) {
			return NAG_109;
		} else if (nagString.equals("$110")) {
			return NAG_110;
		} else if (nagString.equals("$111")) {
			return NAG_111;
		} else if (nagString.equals("$112")) {
			return NAG_112;
		} else if (nagString.equals("$113")) {
			return NAG_113;
		} else if (nagString.equals("$114")) {
			return NAG_114;
		} else if (nagString.equals("$115")) {
			return NAG_115;
		} else if (nagString.equals("$116")) {
			return NAG_116;
		} else if (nagString.equals("$117")) {
			return NAG_117;
		} else if (nagString.equals("$118")) {
			return NAG_118;
		} else if (nagString.equals("$119")) {
			return NAG_119;
		} else if (nagString.equals("$120")) {
			return NAG_120;
		} else if (nagString.equals("$121")) {
			return NAG_121;
		} else if (nagString.equals("$122")) {
			return NAG_122;
		} else if (nagString.equals("$123")) {
			return NAG_123;
		} else if (nagString.equals("$124")) {
			return NAG_124;
		} else if (nagString.equals("$125")) {
			return NAG_125;
		} else if (nagString.equals("$126")) {
			return NAG_126;
		} else if (nagString.equals("$127")) {
			return NAG_127;
		} else if (nagString.equals("$128")) {
			return NAG_128;
		} else if (nagString.equals("$129")) {
			return NAG_129;
		} else if (nagString.equals("$130")) {
			return NAG_130;
		} else if (nagString.equals("$131")) {
			return NAG_131;
		} else if (nagString.equals("$132")) {
			return NAG_132;
		} else if (nagString.equals("$133")) {
			return NAG_133;
		} else if (nagString.equals("$134")) {
			return NAG_134;
		} else if (nagString.equals("$135")) {
			return NAG_135;
		} else if (nagString.equals("$136")) {
			return NAG_136;
		} else if (nagString.equals("$137")) {
			return NAG_137;
		} else if (nagString.equals("$138")) {
			return NAG_138;
		} else if (nagString.equals("$139")) {
			return NAG_139;
		} else {
			return null;
		}
	}

	private String symbol;

	private String nag;

	private Nag(String nag) {
		this.nag = nag;
	}

	private Nag(String nag, String symbol) {
		this.nag = nag;
		this.symbol = symbol;
	}

	public String getNagString() {
		return nag;
	}

	public String getSymbol() {
		return symbol;
	}

	public boolean hasSymbol() {
		return symbol != null;
	}

	public String toString() {
		if (hasSymbol()) {
			return getSymbol();
		} else {
			return name();
		}
	}
}
