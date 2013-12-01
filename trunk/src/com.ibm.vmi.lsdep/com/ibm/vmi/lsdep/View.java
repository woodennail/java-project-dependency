package com.ibm.vmi.lsdep;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class View {
	//p-package/component, r-refclass, c-javaclass, j-jarfile,
	RefClass2JarMap r2jMap=null;
	Class2RefClassMap c2rMap=null;
	HashMap<String, List<String>> pcmap = null;
	HashMap<String, List<String>> prmap=null;
	HashMap<String, List<String>> crmap = null;
	HashMap<String, List<String>> rcmap = null;
	HashMap<String, String> rjmap = null;
	HashMap<String, List<String>> jrmap = null;
	HashMap<String, List<String>> jcmap = null;
	HashMap<String, List<String>> jpmap = null;
	
	public static final String COL_PACKAGE = "component";
	public static final String COL_JARFILE = "jarfile";
	public static final String COL_REFCLASS = "refclass";
	public static final String COL_JAVACLASS = "javaclass";
	
	public View(){
		
	}
	public void createView(String config){
		Properties prop = new Properties(config);
		String jarPathes = prop.getString(Properties.JARPATHES);
		String javaPathes = prop.getString(Properties.JAVAPATHES);
		String allComponents = prop.getString(Properties.COMPONENTS);
		String outputPath = prop.getString(Properties.OUPUTPATH);
		if(outputPath.isEmpty()){
			outputPath = System.getProperty("user.dir");
		}
		
		r2jMap=new RefClass2JarMap();
		c2rMap=new Class2RefClassMap();
		if(!allComponents.isEmpty()){
			c2rMap.setComponents(allComponents.split(";"));
		}
		r2jMap.appendPathes(jarPathes);
		c2rMap.appendPathes(javaPathes);

		createView(r2jMap, c2rMap);
		try {
			dumpView(outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean createView(RefClass2JarMap r2jMap, Class2RefClassMap c2rMap){
		pcmap = c2rMap.getPackage2ClassMap();
		prmap = c2rMap.getPackage2RefClassMap();
		
		crmap = c2rMap.getClass2RefClassMap();
		rcmap = c2rMap.getRefClass2ClassMap();
		initRefClass2JarFileMap(r2jMap);
		initPackage2RefClassMap();
		initJarFile2RefClassMap();
		initJarFile2ClassMap();
		initJarFile2PackageMap();
		return true;
	}
	public boolean dumpView(String path) throws IOException{
		
		createBriefPackageRefClassView(new File(path+File.separator+"pr.xml"));
		createBriefPackageView(new File(path+File.separator+"pjr.xml"));
		createBriefJarView(new File(path+File.separator+"jp.xml"));
		createDetailJarView(new File(path+File.separator+"jrc.xml"));
		createBriefRefClassPackageView(new File(path+File.separator+"rp.xml"));
		createBriefRefClassJarFileView(new File(path+File.separator+"rj.xml"));
		
		createDetailPackageView(new File(path+File.separator+"pjrc.xml"));
		return true;
	}
	//detail jar view : jar, refClass, class
	public boolean createDetailJarView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = jrmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_JARFILE);
		column.add(COL_REFCLASS);
		column.add(COL_JAVACLASS);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all jar files
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String jarFile = entry.getKey();
			List<String> refClasses = entry.getValue();
			items.clear();
			items.add(LsjarUtil.getShortPath(jarFile));
			doc.insert(items);
			for(String refClass:refClasses){
				while(items.size()>1)items.remove(1);
				items.add(refClass);
				doc.insert(items);
				List<String> javaClasses = rcmap.get(refClass);
				if(javaClasses==null)
					continue;
				for(String className:javaClasses){
					while(items.size()>2)items.remove(2);
					items.add(className);
					doc.insert(items);
				}
			}
		}
		doc.flush();
		return true;
	}
	//brief jar view: jar, component
	public boolean createBriefJarView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = jpmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_JARFILE);
		column.add(COL_PACKAGE);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all jar files
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String jarFile = entry.getKey();
			List<String> packages = entry.getValue();
			items.clear();
			items.add(LsjarUtil.getShortPath(jarFile));
			doc.insert(items);
			for(String packageName:packages){
				while(items.size()>1)items.remove(1);
				items.add(packageName);
				doc.insert(items);
			}
		}
		doc.flush();
		return true;
	}
	//detail component view: package, refClass
	public boolean createBriefPackageRefClassView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = prmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_REFCLASS);
		column.add(COL_PACKAGE);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String packageName = entry.getKey();
			List<String> refClasses = entry.getValue();
			items.clear();
			items.add(packageName);
			doc.insert(items);
			for(String refClass:refClasses){
				while(items.size()>1)items.remove(1);
				items.add(refClass);
				doc.insert(items);
			}
		}
		doc.flush();
		return true;
	}
	//detail component view: refClass, package
	public boolean createBriefRefClassPackageView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = rcmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_PACKAGE);
		column.add(COL_REFCLASS);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String refClass = entry.getKey();
			List<String> javaClasses = entry.getValue();
			items.clear();
			items.add(refClass);
			doc.insert(items);
			List<String> packages = new ArrayList<String>();
			
			for(String className:javaClasses){
				String packageName = c2rMap.className2PackageName(className);
				LsjarUtil.append(packages, packageName);
			}
			for(String packageName:packages){
				while(items.size()>1)items.remove(1);
				items.add(packageName);
				doc.insert(items);
			}
		}
		doc.flush();
		return true;
	}
	//detail component view: refClass, jarFile
	public boolean createBriefRefClassJarFileView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, String>> iter = rjmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_PACKAGE);
		column.add(COL_REFCLASS);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all packages
			Map.Entry<String, String> entry = iter.next(); 
			if(entry==null)
				continue;
			String refClass = entry.getKey();
			String JarFile = entry.getValue();
			items.clear();
			items.add(refClass);
			items.add(LsjarUtil.getShortPath(JarFile));
			doc.insert(items);
		}
		doc.flush();
		return true;
	}
	//detail component view: package, jar, refClass
	public boolean createBriefPackageView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = prmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_PACKAGE);
		column.add(COL_JARFILE);
		column.add(COL_REFCLASS);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String packageName = entry.getKey();
			HashMap<String, List<String>> j2rmap = getJarFile2RefClass(entry.getValue());
			if(j2rmap==null)
				continue;
			items.clear();
			items.add(packageName);
			doc.insert(items);
			Iterator<Map.Entry<String, List<String>>> jarIter = j2rmap.entrySet().iterator();
			while (jarIter.hasNext()){//for all JarFile
				Map.Entry<String, List<String>> jarEntry = jarIter.next(); 
				if(jarEntry==null)
					continue;
				String jarFile = jarEntry.getKey();
				while(items.size()>1)items.remove(1);
				items.add(LsjarUtil.getShortPath(jarFile));
				doc.insert(items);
				List<String>refClasses = jarEntry.getValue();
				for(String refClass:refClasses){//for all refClass
					while(items.size()>2)items.remove(2);
					items.add(refClass);
					doc.insert(items);
				}
			}
		}
		doc.flush();
		return true;
	}
	//detail component view: package, jar, refClass, class
	public boolean createDetailPackageView(File file) throws IOException{
		backupFile(file);
		XmlTreeTableDoc doc= new XmlTreeTableDoc(file);
		Iterator<Map.Entry<String, List<String>>> iter = prmap.entrySet().iterator();
		List<String> column = new ArrayList<String>();
		column.add(COL_PACKAGE);
		column.add(COL_JARFILE);
		column.add(COL_REFCLASS);
		column.add(COL_JAVACLASS);
		doc.setColumns(column);
		List<String> items = new ArrayList<String>();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String packageName = entry.getKey();
			HashMap<String, List<String>> j2rmap = getJarFile2RefClass(entry.getValue());
			if(j2rmap==null)
				continue;
			items.clear();
			items.add(packageName);
			doc.insert(items);
			Iterator<Map.Entry<String, List<String>>> jarIter = j2rmap.entrySet().iterator();
			while (jarIter.hasNext()){//for all JarFile
				Map.Entry<String, List<String>> jarEntry = jarIter.next(); 
				if(jarEntry==null)
					continue;
				String jarFile = jarEntry.getKey();
				while(items.size()>1)items.remove(1);
				items.add(LsjarUtil.getShortPath(jarFile));
				doc.insert(items);
				List<String>refClasses = jarEntry.getValue();
				for(String refClass:refClasses){//for all refClass
					while(items.size()>2)items.remove(2);
					items.add(refClass);
					doc.insert(items);
					List<String>classes = getClasses(refClass, packageName);
					for(String className:classes){//for all classes
						while(items.size()>3)items.remove(3);
						items.add(className);
						doc.insert(items);
					}
				}
			}
		}
		doc.flush();
		return true;
	}
	public HashMap<String, List<String>> getJarFile2RefClass(List<String>refClasses){
		if(refClasses==null)
			return null;
		HashMap<String, List<String>> j2rmap = new HashMap<String, List<String>>();
		for(String refClass:refClasses){
			String jarFile = rjmap.get(refClass);
			if(jarFile ==null || jarFile.isEmpty())
				continue;
			List<String> newRefClasses = j2rmap.get(jarFile);
			if(newRefClasses==null){
				newRefClasses=new ArrayList<String>();
				j2rmap.put(jarFile, newRefClasses);
			}
			int i=1;
			LsjarUtil.append(newRefClasses, refClass);
		}
		return j2rmap;
	}
	public List<String> getClasses(String refClass, String packageName){
		if(refClass==null || packageName==null)
			return null;
		List<String> allClasses = rcmap.get(refClass);
		if(allClasses==null)
			return null;
		List<String> packageClasses = pcmap.get(packageName);
		List<String> classes = new ArrayList<String>();
		for(String ac:allClasses){
			boolean findFlag = false;
			if(ac.isEmpty())
				continue;
			for(String pc:packageClasses){
				if(ac==pc){
					findFlag=true;
					break;
				}
			}
			if(findFlag)
				classes.add(ac);
		}
		return classes;
	}
	protected boolean initPackage2RefClassMap(){
		Iterator<Map.Entry<String, List<String>>> iter = prmap.entrySet().iterator();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			
			List<String>refClasses = entry.getValue();
			for(int i=refClasses.size()-1; i>=0; i--){
				String refClass = refClasses.get(i);
				if(rjmap.get(refClass)==null){
					refClasses.remove(i);
				}
			}
		}
		return true;
	}
	protected boolean initRefClass2JarFileMap(RefClass2JarMap r2jMap){
		HashMap<String, String> allrjmap = r2jMap.getRefClass2JarMap();
		rjmap = new HashMap<String, String>();
		Iterator<Map.Entry<String, String>> iter = allrjmap.entrySet().iterator();
		while (iter.hasNext()){//all packages
			Map.Entry<String, String> entry = iter.next(); 
			if(entry==null)
				continue;
			String refClass = entry.getKey();
			if(rcmap.get(refClass) == null)
				continue;
			rjmap.put(refClass, entry.getValue());
		}
		return true;
	}
	protected boolean initJarFile2RefClassMap(){
		jrmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, String>> iter = rjmap.entrySet().iterator();
		while (iter.hasNext()){//all packages
			Map.Entry<String, String> entry = iter.next(); 
			if(entry==null)
				continue;
			String refClass = entry.getKey();
			String jarFile = entry.getValue();
			if(jarFile.isEmpty() || refClass.isEmpty())
				continue;
			List<String>refClasses = jrmap.get(jarFile);
			if(refClasses==null){
				refClasses = new ArrayList<String>();
				jrmap.put(jarFile, refClasses);
			}
			refClasses.add(refClass);
		}
		return true;
	}
	protected boolean initJarFile2ClassMap(){
		jcmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, List<String>>> iter = jrmap.entrySet().iterator();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String jarFile = entry.getKey();
			List<String> refClasses  = entry.getValue();
			if(jarFile.isEmpty() || refClasses.isEmpty())
				continue;
			for(String refClass:refClasses){
				List<String>javaClasses = rcmap.get(refClass);
				List<String>savedJavaClasses = jcmap.get(jarFile);
				if(savedJavaClasses==null){
					savedJavaClasses = new ArrayList();
					jcmap.put(jarFile, savedJavaClasses);
				}
				LsjarUtil.append(savedJavaClasses, javaClasses);
			}
		}
		return true;
	}
	protected boolean initJarFile2PackageMap(){
		jpmap = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, List<String>>> iter = jcmap.entrySet().iterator();
		while (iter.hasNext()){//all packages
			Map.Entry<String, List<String>> entry = iter.next(); 
			if(entry==null)
				continue;
			String jarFile = entry.getKey();
			List<String> javaClasses  = entry.getValue();
			if(jarFile.isEmpty() || javaClasses.isEmpty())
				continue;
			for(String className:javaClasses){
				String packageName = c2rMap.className2PackageName(className);
				List<String>packages = jpmap.get(jarFile);
				if(packages==null){
					packages = new ArrayList();
					jpmap.put(jarFile, packages);
				}
				LsjarUtil.append(packages, packageName);
			}
		}
		return true;
	}
	//brief component view: package, jar
	public boolean createBriefPackageViewBrief(File file){
		return false;
	}
	protected static boolean backupFile(File file){
		if(file.exists()){
			File bkFile = new File(file.getAbsolutePath()+".bk");
			if(!file.renameTo(bkFile)){
				file.delete();
			}
			return true;
		}
		else
			return true;
	}
	
}
