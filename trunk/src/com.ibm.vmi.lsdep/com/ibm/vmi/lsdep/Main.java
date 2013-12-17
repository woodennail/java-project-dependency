package com.ibm.vmi.lsdep;

import java.io.IOException;


public class Main {

	/**
	 * @param args
	 */
	static String configFile = "";
	/*
	public static boolean test(){
		if(0==0)
			return true;
		Class2RefClassMap crmap=new Class2RefClassMap();
		try {
			crmap.append(new java.io.File("E:\\\\workspace\\vmi\\src\\com.ibm.vmi.updates.appliance\\java\\src\\com\\ibm\\vmi\\updates\\appliance\\UpdateAppliance.java"));
			System.out.println("hello");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}*/
	public static boolean paraParse(String[] args){
		String help=Properties.getResource("help");
		configFile = "";
		for(int i=0; i<args.length; i++){
			String arg=args[i];
			if(arg.equals("-h") || arg.equals("--help")){
				System.out.println(help);
				return false;
			}
			else if(arg.equals("-f")){
				if(args.length <= i+1 || !configFile.isEmpty()){
					System.out.println(help);
					return false;
				}
				configFile = args[++i];
			}
			else{
				System.out.println(help);
				return false;
			}
		}
		//System.out.println(help);
		return true;
	}
	public static void main(String[] args) {
		if(!paraParse(args))
			return;
		View view=new View();
		view.createView(configFile);
		try {
			view.createDetailPackageView(new java.io.File("E:\\projects\\lsdependjar\\out\\detail.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
