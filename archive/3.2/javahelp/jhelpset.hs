<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0">
  <title>Geometria 3.0</title>
  <maps>
    <homeID>top</homeID>
    <mapref location="jhelpmap.jhm"/>
  </maps>
  <view>
    <name>TOC</name>
    <label>Table Of Contents</label>
    <type>javax.help.TOCView</type>
    <data>jhelptoc.xml</data>
  </view>
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
  </view>

  <presentation default=true>
    <name>mainWindow</name>
    <location x="100" y="100"/>
    <size width="700" height="600"/>
    <toolbar>
      <helpaction image="backicon">javax.help.BackAction</helpaction>
      <helpaction image="forwardicon">javax.help.ForwardAction</helpaction>
      <helpaction image="homeicon">javax.help.HomeAction</helpaction>
      <helpaction image="printicon">javax.help.PrintAction</helpaction>
    </toolbar>
  </presentation>
</helpset>
