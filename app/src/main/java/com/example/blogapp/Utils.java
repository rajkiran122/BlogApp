package com.example.blogapp;

import java.util.regex.Pattern;

public class Utils
{

    public static final Pattern PASSWORD_UPPERCASE_PATTERN=
            Pattern.compile("(?=.*[A-Z])"+".{0,}");

    public static final Pattern PASSWORD_LOWERCASE_PATTERN=
            Pattern.compile("(?=.*[a-z])"+".{0,}");

    public static final Pattern PASSWORD_NUMBER_PATTERN=
            Pattern.compile("(?=.*[0-9])"+".{0,}");



}