package com.siyeh.igtest.performance.redundant_string_format_call;





public class RedundantStringFormatCall {

    public static final String A = String.format("%n");
  String b = String.format("no parameters");
  String c = String.format("asdf%n" +
                           "asdf%n")

}