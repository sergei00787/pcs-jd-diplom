import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> wordsMap = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] files = pdfsDir.listFiles();

        if (files == null) return;

        for (File file : files) {
            var doc = new PdfDocument(new PdfReader(file));
            int numberOfPAge = doc.getNumberOfPages();

            for (int i = 1; i < numberOfPAge; i++) {

                var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = getMapWordsIndex(words);
                fillWordsMap(file, i, freqs);
            }
        }
    }

    private void fillWordsMap(File file, int i, Map<String, Integer> freqs) {
        for (var entry : freqs.entrySet()) {
            if (wordsMap.containsKey(entry.getKey())) {
                wordsMap.get(entry.getKey()).add(new PageEntry(file.getName(), i, entry.getValue()));
            } else {
                wordsMap.put(entry.getKey(), new ArrayList<>(List.of(new PageEntry(file.getName(), i, entry.getValue()))));
            }
        }
    }

    private Map<String, Integer> getMapWordsIndex(String[] words) {
        Map<String, Integer> freqs = new HashMap<>();
        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
        }
        return freqs;
    }

    @Override
    public List<PageEntry> search(String word) {
        if (wordsMap.get(word) == null) return null;
        List<PageEntry> list = wordsMap.get(word);
        Collections.sort(list);
        return list;
    }
}
