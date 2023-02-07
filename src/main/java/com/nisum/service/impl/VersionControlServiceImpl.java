package com.nisum.service.impl;

import com.nisum.domain.TagLinesDetails;
import com.nisum.service.VersionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile({"dev", "qa"})
public class VersionControlServiceImpl implements VersionControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionControlServiceImpl.class);

    public Set<String> prepareTagsWithFileNumbersMap(List<TagLinesDetails> tagLinesDetailsList, int tagLinesCount, int parallelBuildsCount) throws RuntimeException {
        LOGGER.info("Preparing tags with files numbers");
        Set<String> featureFiles = new HashSet<>();
        LOGGER.info("Total Lines Count : " + tagLinesCount);
        LOGGER.info("Builds Needed : " + parallelBuildsCount);

        int tagLinesPerBuild = (tagLinesCount / parallelBuildsCount);
        List<Integer> testsCountPerBuild = new ArrayList<>();
        for (int i = 0; i < parallelBuildsCount; i++) {
            testsCountPerBuild.add(tagLinesPerBuild);
        }
        for (int i = 0; i < tagLinesCount - (tagLinesPerBuild * parallelBuildsCount); i++) {
            testsCountPerBuild.set(i, testsCountPerBuild.get(i) + 1);
        }

        //Compare by tag name and then file path
        Comparator<TagLinesDetails> comparator = Comparator.comparing(TagLinesDetails::getTagName).thenComparing(TagLinesDetails::getFilePath);

        List<TagLinesDetails> sortedTagLinesDetailsList = tagLinesDetailsList.stream().sorted(comparator).collect(Collectors.toList());
        TagLinesDetails tagLinesDetails = null;
        int buildCount = 0;
        for (int index = 0; buildCount < parallelBuildsCount; ) {
            HashMap<String, String> buildSpecificScenarios = new HashMap<>();
            for (int linesCount = 1; index < tagLinesCount && linesCount <= testsCountPerBuild.get(buildCount); linesCount++, index++) {
                tagLinesDetails = sortedTagLinesDetailsList.get(index);
                if (buildSpecificScenarios.containsKey(tagLinesDetails.getFilePath())) {
                    buildSpecificScenarios.put(tagLinesDetails.getFilePath(), buildSpecificScenarios.get(tagLinesDetails.getFilePath()) + ":" + tagLinesDetails.getLineNumber());
                } else {
                    buildSpecificScenarios.put(tagLinesDetails.getFilePath(), tagLinesDetails.getFilePath() + ":" + tagLinesDetails.getLineNumber());
                }
            }
            String commaSeparatedFeatureFiles = buildSpecificScenarios.values().stream().collect(Collectors.joining(","));
            featureFiles.add(commaSeparatedFeatureFiles);
            LOGGER.info("Feature File Tag Lines for Build-{} : {}", ++buildCount, commaSeparatedFeatureFiles);
        }
        return featureFiles;
    }

}
