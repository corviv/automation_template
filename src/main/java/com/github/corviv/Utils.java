package com.github.corviv;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.IntStream;

@Listeners(LoggerListener.class)
public class Utils {

    /* Examples @DataProvider methods for MyDataEnumerator class:

    @DataProvider(name = "MyDataEnumerator")
    public Iterator<Object[]> MyDataEnumerator() {
        // ranges must be proportionate!
        // (e_enum - b_enum) == (e_data - b_data)
        return new Utils.MyDataEnumerator(1, 5, 20, 25);
    }

    @DataProvider(name = "MyDataEnumerator2")
    public Iterator<Object[]> MyDataEnumerator() {
        return new Utils.MyDataEnumerator(1, 5, 10, 20, false);
    }

    @DataProvider(name = "MyDataEnumerator3")
    public Iterator<Object[]> MyDataEnumerator2() {
        return new Utils.MyDataEnumerator(1, 5);
    }
    */

    private static final Logger logger = LoggerFactory.getLogger("Utils");

    public static class MyDataEnumerator implements Iterator<Object[]> {

        private int[] enum_data = null;
        private int[] data = null;
        private boolean isEnumAll = true;
        private int index = 0;
        private static final Logger logger = LoggerFactory.getLogger("MyDataEnumerator");

        public MyDataEnumerator(int b_enum, int e_enum) {
            enum_data = IntStream.rangeClosed(b_enum, e_enum).toArray();
        }

        public MyDataEnumerator(int b_enum, int e_enum, int b_data, int e_data){
            enum_data = IntStream.rangeClosed(b_enum, e_enum).toArray();
            data = IntStream.rangeClosed(b_data, e_data).toArray();
        }

        public MyDataEnumerator(int b_enum, int e_enum, int b_data, int e_data, boolean isEnumAll){
            enum_data = IntStream.rangeClosed(b_enum, e_enum).toArray();
            data = IntStream.rangeClosed(b_data, e_data).toArray();
            this.isEnumAll = isEnumAll;
        }

        @Override
        public boolean hasNext() {
            return (index <= (enum_data.length - 1));
        }

        @Override
        public Object[] next() {
            if (data == null)
                return new Object[]{enum_data[index++]};
            else if (isEnumAll)
                return new Object[]{enum_data[index], data[index++]};
            else
                return new Object[]{enum_data[index++], data};
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Removal of items is not supported");
        }
    }

    static int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.ints(min, (max + 1)).findFirst().getAsInt();
    }

    static double getRandomNumberInRange(double min, double max) {
        Random r = new Random();
        return r.doubles(min, (max + 1)).findFirst().getAsDouble();
    }

    public static boolean compareLists(String[] expectedList, String actual, String delimiter) {
        Arrays.sort(expectedList);
        String[] actualList = Iterables.toArray(Splitter.on(delimiter).split(actual), String.class);
        Arrays.sort(actualList);
        Boolean compare = Arrays.equals(expectedList, actualList);
        if (!compare) {
            for (int i = 0; i<actualList.length; i++) {
                logger.info("expected: " + expectedList[i] + " found: " + actualList[i]);
            }
        }
        return Arrays.equals(expectedList, actualList);
    }
}
