package uk.gov.defra.datareturns.util;

import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.common.processor.ObjectRowListProcessor;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
    }


    /**
     * Read CSV data from the given {@link java.io.InputStream}
     */
    public static CsvReadResult<Object[]> read(final InputStream stream) {
        final ObjectRowListProcessor rowProcessor = new ObjectRowListProcessor();
        final CsvReadResult<Object[]> result = new CsvReadResult<Object[]>() {
            @Override
            public String[] getHeaders() {
                return rowProcessor.getHeaders();
            }

            @Override
            public List<Object[]> getRows() {
                return rowProcessor.getRows();
            }
        };
        return parse(result, rowProcessor, stream);
    }

    private static <T> CsvReadResult<T> parse(final CsvReadResult<T> resultHandler, final RowProcessor processor, final InputStream stream) {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(processor);
        final CsvParser parser = new CsvParser(settings);
        parser.parse(stream);
        return resultHandler;
    }

    /**
     * Writes CSV data to the given {@link HttpServletResponse}
     *
     * @param beanClass the class of the source bean whose data should be written
     * @param entries   a {@link Iterable} of beans that should be serialised to CSV
     * @param response  the {@link HttpServletResponse} to write to
     * @param <T>       the generic type of the beans to be written
     * @throws IOException if an IO error occurs
     */
    public static <T> void writeCsv(final Class<T> beanClass, final Iterable<T> entries, final HttpServletResponse response) throws IOException {
        writeCsv(beanClass, entries, response, beanClass.getSimpleName() + ".csv");
    }

    /**
     * Writes CSV data to the given {@link HttpServletResponse}
     *
     * @param beanClass the class of the source bean whose data should be written
     * @param entries   a {@link Iterable} of beans that should be serialised to CSV
     * @param response  the {@link HttpServletResponse} to write to
     * @param filename  the filename for the output file
     * @param <T>       the generic type of the beans to be written
     * @throws IOException if an IO error occurs
     */
    public static <T> void writeCsv(final Class<T> beanClass, final Iterable<T> entries, final HttpServletResponse response, final String filename)
            throws IOException {
        final CsvWriterSettings settings = new CsvWriterSettings();
        settings.setAutoConfigurationEnabled(true);
        settings.setHeaderWritingEnabled(true);
        settings.setRowWriterProcessor(new BeanWriterProcessor<>(beanClass));
        try (final Writer writer = response.getWriter()) {
            response.setHeader("Content-Type", "text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            final CsvWriter csvWriter = new CsvWriter(writer, settings);
            csvWriter.writeHeaders();
            csvWriter.processRecords(entries);
            csvWriter.flush();
            csvWriter.close();
        }
    }


    public interface CsvReadResult<T> {
        String[] getHeaders();

        List<T> getRows();
    }
}
