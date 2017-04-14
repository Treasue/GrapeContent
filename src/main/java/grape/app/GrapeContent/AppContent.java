package grape.app.GrapeContent;

import httpServer.booter;

public class AppContent {
	public static void main(String[] args) {
		booter booter = new booter();
		System.out.println("GrapeContent！");
		try {
			System.setProperty("AppName", "GrapeContent");
			booter.start(6003);
		} catch (Exception e) {
			
		}
	}
}
