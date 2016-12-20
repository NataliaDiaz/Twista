/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package daw;

/**
 *
 * @author User
 */
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapUtils {
  /***
   * @author frichie Sorting map by its values
   */
  private static class DescValueComparer<K, V extends Comparable> implements Comparator {
    protected final Map<K, V> map;

    public DescValueComparer(Map map) {
      this.map = map;
    }

    public int compare(Object key1, Object key2) {
      V value1 = this.map.get(key1);
      V value2 = this.map.get(key2);
      int c = value1.compareTo(value2);
      if (c != 0)
        return -c;
      Integer hashCode1 = key1.hashCode();
      Integer hashCode2 = key2.hashCode();
      return -hashCode1.compareTo(hashCode2);
    }
  }

  private static class IncValueComparer<K, V extends Comparable> extends DescValueComparer {
    public IncValueComparer(Map map) {
      super(map);
    }

    public int compare(Object key1, Object key2) {
      return -super.compare(key1, key2);
    }
  }

  public static <K, V> SortedMap<K, V> sortMapByValue(Map<K, V> map) {
    return sortMapByValue(map, true);
  }

  public static <K, V> SortedMap<K, V> sortMapByValue(Map<K, V> map, boolean descending) {
    Comparator<?> vc = null;
    if (descending)
      vc = new DescValueComparer(map);
    else
      vc = new IncValueComparer(map);
    SortedMap sm = new TreeMap(vc);
    sm.putAll(map);
    return sm;
  }

  public static void main(String[] a) {
    Map<String, Integer> m = new HashMap<String, Integer>();
    m.put("a", 3);
    m.put("b", 2);
    m.put("b", 4);
    m.put("d", 4);
    m.put("c", -4);
    System.out.println(sortMapByValue(m));
  }

  /***
   * Add value to a map with numeric value
   */
  public static <K, T extends Number> void addToMap(Map<K, T> m, K key, T value) {
    m.put(key, m.containsKey(key) ? (T) (Double) (value.doubleValue() + m.get(key).doubleValue()) : value);
  }

  public static <K> void addToMap(Map<K, Double> m, K key, Double value) {
    m.put(key, m.containsKey(key) ? value + m.get(key) : value);
  }

  public static <K> void addToMap(Map<K, Integer> m, K key, Integer value) {
    m.put(key, m.containsKey(key) ? value + m.get(key) : value);
  }

}

