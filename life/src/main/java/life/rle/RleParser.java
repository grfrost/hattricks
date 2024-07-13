package life.rle;

import life.rle.helper.StatusRunHelper;
import life.rle.domain.Coordinate;
import life.rle.domain.LiveCells;
import life.rle.domain.MetaData;
import life.rle.domain.PatternData;
import life.rle.domain.StatusRun;
import life.rle.domain.enumeration.Status;
import life.rle.helper.FileHelper;
import life.rle.helper.RleFileHelper;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RleParser {

    private static final String META_DATA_WIDTH_KEY = "x";
    private static final String META_DATA_HEIGHT_KEY = "y";
    private static final String META_DATA_RULE_KEY = "rule";
    private static final String ENCODED_CELL_DATA_TERMINATOR = "!";

    private static final String ENCODED_CELL_DATA_LINE_SEPARATOR_REGEX = "\\$";

    private static final String ENCODED_RUN_LENGTH_STATUS_REGEX = "\\d*[a-z$]";
    private static final Pattern ENCODED_STATUS_RUN_PATTERN = Pattern.compile(ENCODED_RUN_LENGTH_STATUS_REGEX);

    public static PatternData readPatternData(URI rleFileUri) {
        List<String> lines = FileHelper.getFileAsStringList(rleFileUri);

        List<String> trimmedNonEmptyNonCommentLines = lines.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(string -> !string.isEmpty())
                .filter(string -> !RleFileHelper.isCommentString(string))
                .collect(Collectors.toList());

        if (trimmedNonEmptyNonCommentLines.size() == 0)
            throw new IllegalArgumentException("No non-comment, non-empty lines in RLE file");

        MetaData metaData = readMetaData(trimmedNonEmptyNonCommentLines.get(0));
        LiveCells liveCells = extractLiveCells(
                metaData,
                trimmedNonEmptyNonCommentLines.stream()
                        .skip(1)
                        .collect(Collectors.joining())
        );

        return new PatternData(metaData, liveCells);
    }

    private static MetaData readMetaData(String line) {
        String lineWithoutWhiteSpace = line.replaceAll("\\s", "");
        String[] metaDataProperties = lineWithoutWhiteSpace.split(",");

        if (metaDataProperties.length < 2)
            throw new IllegalArgumentException("RLE file header has less than two properties");

        Map<String, String> metaDataPropertyKeyValueMap = Arrays.stream(metaDataProperties)
                .map(metaDataProperty -> metaDataProperty.split("="))
                .map(metaDataPropertyKeyValueArray -> {
                    if (metaDataPropertyKeyValueArray.length < 2)
                        return new String[]{metaDataPropertyKeyValueArray[0], null};
                    else
                        return metaDataPropertyKeyValueArray;
                })
                .collect(Collectors.toMap(metaDataPropertyKeyValueArray -> metaDataPropertyKeyValueArray[0], metaDataPropertyKeyValueArray -> metaDataPropertyKeyValueArray[1]));

        String width = metaDataPropertyKeyValueMap.get(META_DATA_WIDTH_KEY);
        String height = metaDataPropertyKeyValueMap.get(META_DATA_HEIGHT_KEY);
        String rule = metaDataPropertyKeyValueMap.get(META_DATA_RULE_KEY);

        return new MetaData(Integer.parseInt(width), Integer.parseInt(height), rule);
    }

    private static LiveCells extractLiveCells(MetaData metaData, String rleCellData) {
        if (metaData.getWidth() == 0 && metaData.getHeight() == 0)
            if (rleCellData.length() > 0)
                throw new IllegalArgumentException("RLE header has width 0 and height 0 but there are lines after it");
            else
                return new LiveCells(new HashSet<>());
        else if (rleCellData.length() == 0)
            throw new IllegalArgumentException("RLE header has width > 0 and height > 0 but there are no lines after it");
        else if (!rleCellData.contains(ENCODED_CELL_DATA_TERMINATOR))
            throw new IllegalArgumentException("RLE pattern did not contain terminating character '!'");
        else {
            String encodedCellData = rleCellData.substring(0, rleCellData.indexOf(ENCODED_CELL_DATA_TERMINATOR));
            Matcher matcher = ENCODED_STATUS_RUN_PATTERN.matcher(encodedCellData);
            List<StatusRun> statusRuns = new ArrayList<>();
            Coordinate coordinate = new Coordinate(0, 0);

            while (matcher.find()) {
                StatusRun statusRun = StatusRunHelper.readStatusRun(matcher.group(), coordinate);

                if (Status.LINE_END.equals(statusRun.getStatus()))
                    coordinate = coordinate.withX(0).plusToY(statusRun.getLength());
                else {
                    coordinate = coordinate.plusToX(statusRun.getLength());

                    if (Status.ALIVE.equals(statusRun.getStatus()))
                        statusRuns.add(statusRun);
                }
            }

            Set<Coordinate> coordinates = statusRuns.stream()
                    .map(StatusRunHelper::readCoordinates)
                    .reduce(new HashSet<>(), (coordinateAccumulator, coordinateSet) -> {
                        coordinateAccumulator.addAll(coordinateSet);
                        return coordinateAccumulator;
                    });

            return new LiveCells(coordinates);
        }
    }
}
