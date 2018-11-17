package be.bagofwords.text;

import be.bagofwords.util.Direction;

import java.util.Set;

/**
 * Split a text into words.
 */

public class WordIterator {

    public static final int MAX_LENGTH_OF_WORD = 100;

    private static final char FULL_STOP = '.';

    private BowStringImpl nextWord;
    private int pos;
    private final Text text;
    private final Set<String> wordsWithPunct;

    public WordIterator(String text, Set<String> wordsWithPunct) {
        this(new TransientText(text), wordsWithPunct);
    }

    public WordIterator(Text text, Set<String> wordsWithPunct) {
        this.text = text;
        this.pos = 0;
        this.wordsWithPunct = wordsWithPunct;
        findNextWord();
    }

    private void findNextWord() {
        nextWord = findWord(text, pos, Direction.Right, wordsWithPunct);
        if (nextWord != null) {
            pos = nextWord.end;
        }
    }

    public static BowStringImpl findWord(Text text, int startOfSearch, Direction direction, Set<String> wordsWithPunct) {
        BowStringImpl nextWord = findWord(text, startOfSearch, direction, true);
        if (nextWord != null && containsNonLetterOrNumber(nextWord)) {
            if (wordsWithPunct.contains(nextWord.toString())) {
                return nextWord;
            } else {
                return findWord(text, startOfSearch, direction, false);
            }
        }
        return nextWord;
    }


    private static boolean containsNonLetterOrNumber(BowStringImpl nextWord) {
        for (int i = 0; i < nextWord.length(); i++) {
            if (!Character.isLetterOrDigit(nextWord.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static BowStringImpl findWord(Text text, int startOfSearch, Direction direction, boolean allowPossibleWordChars) {
        int pos = startOfSearch;
        if (direction == Direction.Right) {
            while (pos < text.length() && isNonWordChar(text.charAt(pos), false)) {
                pos++;
            }
            int start = pos;
            while (pos - start < MAX_LENGTH_OF_WORD && pos < text.length() && !isNonWordChar(text.charAt(pos), allowPossibleWordChars)) {
                pos++;
            }
            if (start < pos) {
                BowStringImpl result = new BowStringImpl(text, start, pos);
                return result;
            } else
                return null;
        } else {
            pos--;
            while (pos >= 0 && isNonWordChar(text.charAt(pos), false)) {
                pos--;
            }
            int start = pos;
            while (start - pos < MAX_LENGTH_OF_WORD && pos >= 0 && !isNonWordChar(text.charAt(pos), allowPossibleWordChars)) {
                pos--;
            }
            if (start > pos)
                return new BowStringImpl(text, pos + 1, start + 1);
            else
                return null;
        }
    }

    public static boolean isNonWordChar(char c, boolean allowPossibleWordChars) {
        if (Character.isLetterOrDigit(c)) {
            return false;
        } else {
            return !(allowPossibleWordChars && c == FULL_STOP);
        }
    }

    public boolean hasNext() {
        return nextWord != null;
    }

    public BowStringImpl next() {
        BowStringImpl result = nextWord;
        findNextWord();
        return result;
    }

    public void reset() {
        pos = 0;
        findNextWord();
    }

}
