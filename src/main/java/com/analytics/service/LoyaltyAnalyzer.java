package com.analytics.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Finds loyal customers across two daily log files. */
public interface LoyaltyAnalyzer {

    List<String> find(Path dayX, Path dayY) throws IOException;

}
