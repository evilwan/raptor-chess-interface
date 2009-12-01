package raptor.util;

import java.io.File;

import raptor.Raptor;

public class ConvertIcons {

	public static final String[][] IMAGE_MAP = {
			{ "alarmClock.png", "18.png" }, { "back.png", "56.png" },
			{ "barGraph.png", "11.png" }, { "calculator.png", "41.png" },
			{ "calendar.png", "43.png" }, { "camera.png", "66.png" },
			{ "cd.png", "21.png" }, { "chat.png", "48.png" },
			{ "chat2.png", "49.png" }, { "clipboard.png", "63.png" },
			{ "clockwise.png", "75.png" }, { "cloredPieGraph.png", "74.png" },
			{ "content.png", "8.png" }, { "counterClockwise.png", "76.png" },
			{ "dimLightbulb.png", "4.png" }, { "down.png", "54.png" },
			{ "draw.png", "69.png" }, { "enter.png", "56.png" },
			{ "fire.png", "31.png" }, { "first.png", "45.png" },
			{ "flip.png", "42.png" }, { "folderOpen.png", "53.png" },
			{ "frowny.png", "7.png" }, { "funnel.png", "61.png" },
			{ "greenPlus.png", "13.png" }, { "home.png", "17.png" },
			{ "key.png", "72.png" }, { "last.png", "46.png" },
			{ "leftright.png", "71.png" }, { "lineGraph.png", "32.png" },
			{ "litLightbulb.png", "3.png" }, { "locked.png", "27.png" },
			{ "minusInBox.png", "59.png" }, { "monitor.png", "23.png" },
			{ "moveList.png", "64.png" }, { "musicNote.png", "47.png" },
			{ "next.png", "57.png" }, { "northEast.png", "34.png" },
			{ "northEastTwist.png", "45.png" }, { "northWest.png", "35.png" },
			{ "northWestTwist.png", "45.png" }, { "oldipod.png", "39.png" },
			{ "paperFoldedTopRight.png", "1.png" },
			{ "paperFoldedTopRightLines.png", "2.png" },
			{ "pieChart.png", "74.png" }, { "plusInBox.png", "60.png" },
			{ "printer.png", "16.png" }, { "puzzlePiece.png", "68.png" },
			{ "redFlag.png", "20.png" }, { "redHeart.png", "15.png" },
			{ "redMinus.png", "14.png" }, { "redx.png", "33.png" },
			{ "rotatedBarGraph.png", "12.png" }, { "save.png", "22.png" },
			{ "search.png", "65.png" }, { "scroll.png", "62.png" },
			{ "shield.png", "30.png" }, { "smiley.png", "6.png" },
			{ "smiley2.png", "5.png" }, { "southEast.png", "37.png" },
			{ "southWest.png", "36.png" }, { "sphere.png", "77.png" },
			{ "stickOutTounge.png", "10.png" }, { "stickyNote.png", "26.png" },
			{ "sun.png", "70.png" }, { "talking.png", "9.png" },
			{ "unlocked.png", "28.png" }, { "up.png", "55.png" },
			{ "wrench.png", "73.png" }, { "yellowStar.png", "25.png" } };

	public static void main(String args[]) throws Exception {
		File largeIcons = new File(Raptor.RESOURCES_DIR + "icons/large/");
		largeIcons.mkdir();

		File mediumIcons = new File(Raptor.RESOURCES_DIR + "icons/medium/");
		mediumIcons.mkdir();

		File smallIcons = new File(Raptor.RESOURCES_DIR + "icons/small/");
		smallIcons.mkdir();

		File tinyIcons = new File(Raptor.RESOURCES_DIR + "icons/tiny/");
		tinyIcons.mkdir();

		@SuppressWarnings("unused")
		File tinySource = new File(
				"/Users/mindspan/desktop/12x12-free-toolbar-icons/png");
		File smallSource = new File(
				"/Users/mindspan/desktop/16x16-free-toolbar-icons/png");
		File mediumSource = new File(
				"/Users/mindspan/desktop/20x20-free-toolbar-icons/png/20x20");
		File largeSource = new File(
				"/Users/mindspan/desktop/24x24-free-toolbar-icons/png/24x24");

		for (String[] mapping : IMAGE_MAP) {
			// FileUtils.copyFiles(new File(tinySource, mapping[1]), new File(
			// tinyIcons, mapping[0]));
			FileUtils.copyFiles(new File(smallSource, mapping[1]), new File(
					smallIcons, mapping[0]));
			FileUtils.copyFiles(new File(mediumSource, mapping[1]), new File(
					mediumIcons, mapping[0]));
			FileUtils.copyFiles(new File(largeSource, mapping[1]), new File(
					largeIcons, mapping[0]));

			System.err.println("Processed " + mapping[0]);

		}

	}
}
