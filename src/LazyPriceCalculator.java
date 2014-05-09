import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;


public class LazyPriceCalculator {
	private static String[][] SamplePricing =  new String[][] { 
		{ "A", "1", "2" }, { "A", "4", "7" },
		{ "B", "1", "12" }, { "C", "1", "1.25" },
		{ "C", "6", "6" }, { "D", "1", "0.15" } };
	private HashMap<Character, TreeMap<Integer, Float>> priceMap = null;
	private HashMap<Character, Integer> purchaseMap = null;
	private StringBuffer input = null;
	public Float total = null;

	/**
	 * 
	 * @param pricePolicy
	 *            a raw 2nd array of string to generate price policy map. 
	 *            You can inject price policy using this. 
	 *            ex) sample = new String[][]{ 
	 *            	{ "A", "1", "2" }, { "A", "4", "7" }, 
	 *              { "B", "1", "12" }, 
	 *            	{ "C", "1", "1.25" }, { "C", "6", "6" }, 
	 *              { "D", "1", "0.15" } 
	 *            };
	 */
	public LazyPriceCalculator(String[][] pricePolicy) {
		if (pricePolicy == null) {
			pricePolicy = LazyPriceCalculator.SamplePricing;
		}
		setPricing(pricePolicy);
		input = new StringBuffer();
		total = new Float(0);
		purchaseMap = new HashMap<Character, Integer>();
	}
	
	public LazyPriceCalculator() {
		input = new StringBuffer();
		total = new Float(0);
		purchaseMap = new HashMap<Character, Integer>();
	}
	
	public void setPricing(String[][] pricePolicy) {
		priceMap = getPriceMap(pricePolicy);
	}
	
	public void scan(Character product) {
		scanOnly(product);
		Integer value = purchaseMap.get(product);
		if (value != null) {
			purchaseMap.put(product, value + 1);
		} else {
			purchaseMap.put(product, 1);
		}
		getTotalPrice();
	}
	
	public void scanOnly(Character product) {
		input.append(product);
	}

	/**
	 * get total price of all products in input
	 * @return total price of input
	 */

	public Float getTotalPrice() {
		total = new Float(0);
		HashMap<Character, Integer> purchaseMap = getPurchaseMap();
		for (Entry<Character, Integer> purchase : purchaseMap.entrySet()) {
			TreeMap<Integer, Float> unitPriceMap = priceMap.get(purchase
					.getKey());
			total += getPrice(purchase, unitPriceMap);
		}
		return total;
	}
	
	public void refreshTotalPrice() {
		
	}

	/**
	 * calculate a total price of a product purchase
	 * 
	 * @param purchase
	 *            a pair(Entry) of product name and the number of purchase
	 * @param unitPriceMap
	 *            a price policy map correspond to the product
	 * @return total price
	 */
	private Float getPrice(Entry<Character, Integer> purchase,
			TreeMap<Integer, Float> unitPriceMap) {
		Integer purchaseCount = purchase.getValue();
		Float purchasePrice = new Float(0);
		// System.out.println(purchase.getKey() + ": ");
		for (Entry<Integer, Float> unitPrice : unitPriceMap.entrySet()) {
			if (purchaseCount == 0) {
				break;
			}
			Integer unit = unitPrice.getKey();
			Float price = unitPrice.getValue();
			Integer unitCount = Math.round(purchaseCount / unit);
			purchaseCount = purchaseCount % unit;
			purchasePrice += unitCount * price;
		}
		// System.out.println(purchasePrice);
		return purchasePrice;
	}

	/**
	 * generate a map that contains price info each product can have multiple
	 * unit-price entry
	 * 
	 * @return a map, key is product name, value is a price policy map for the
	 *         product
	 */
	private HashMap<Character, TreeMap<Integer, Float>> getPriceMap(
			String[][] sample) {
		HashMap<Character, TreeMap<Integer, Float>> map = new HashMap<Character, TreeMap<Integer, Float>>();
		for (int i = 0; i < sample.length; i++) {
			String[] priceSet = sample[i];
			Character key = priceSet[0].charAt(0); // casting to character
			Integer unitCount = Integer.valueOf(priceSet[1]);
			Float price = Float.valueOf(priceSet[2]);

			// setting up each price map
			TreeMap<Integer, Float> priceMap = map.get(key);
			//if it's first time, init new priceMap
			if (priceMap == null) {
				// make tree map reverse order
				priceMap = new TreeMap<Integer, Float>(Collections.reverseOrder());
				priceMap.put(unitCount, price);
				map.put(key, priceMap);
			} else {
				priceMap.put(unitCount, price); 
			}
		}
		return map;
	}

	/**
	 * generate purchase info as a Map. 
	 * 
	 * @param input
	 *            follows the requirement, list of capital characters only. ex)
	 *            ABCDABAA
	 * @return a map, key is a product name, value is number of purchase
	 */
	private HashMap<Character, Integer> getPurchaseMap() {
		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		for (int i = 0; i < input.length(); i++) {
			Character key = input.charAt(i);
			Integer value = map.get(key);
			if (value != null) {
				map.put(key, value + 1);
			} else {
				map.put(key, 1);
			}
		}
		return map;
	}

	public static void main(String[] args) {
		ArrayList<String> inputs = new ArrayList<String>(Arrays.asList(
				"ABCDABAA", "CCCCCCC", "ABCD", 
				"ABDCDCBACBACBDBCBCBBBBACBDBCBACBCBBBCBABBCDBABCBABDCBADBCBBABDCBABCADBCBADBABBABBAAAAAABBCABCABCABCABCABCBABCBCBCBDBDBDDDDDDDDDDDDD"));
		ArrayList<Float> expected = new ArrayList<Float>(Arrays.asList(
				new Float(32.40), new Float(7.25), new Float(15.40), new Float(703.5)));
		try {
			// this - test function inside of its class - is not normal, 
			// but wrote it for convenience.
			PriceCalculator.testGetTotalPrice(inputs, expected);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	public void simulateScan(String input) {
		for (int i=0; i<input.length() ; i++) {
			this.scan(input.charAt(i));
		}
	}

	public static boolean testGetTotalPrice(ArrayList<String> inputList,
			ArrayList<Float> expectedList) throws Exception {
		if (inputList.size() != expectedList.size() ) {
			throw new Exception("the length of expectedList should match to the length of inputList");
		}
		long startTime = System.currentTimeMillis();
		for (int j=0; j<1000 ; j++) {
		for (int i=0; i<inputList.size() ; i++) {
			String input = inputList.get(i);
			Float expected = expectedList.get(i);
			
			PriceCalculator calc = new PriceCalculator();
			calc.setPricing(LazyPriceCalculator.SamplePricing);
			calc.simulateScan(input);
			//calc.getTotalPrice();
			Float totalPrice = calc.total;
			if (Float.compare(totalPrice, expected) !=0) {
				throw new Exception("test failed at index: " + i + "\nexpected: " + expected + "\nresult: " + totalPrice);
			}
		}
		}
		long endTime = System.currentTimeMillis();
		
		long startTime1 = System.currentTimeMillis();
		for (int j=0; j<1000 ; j++) {
		for (int i=0; i<inputList.size() ; i++) {
			String input = inputList.get(i);
			Float expected = expectedList.get(i);
			
			PriceCalculator calc = new PriceCalculator();
			calc.setPricing(LazyPriceCalculator.SamplePricing);
			calc.simulateScan(input);
			//calc.getTotalPrice();
			Float totalPrice = calc.total;
			if (Float.compare(totalPrice, expected) !=0) {
				throw new Exception("test failed at index: " + i + "\nexpected: " + expected + "\nresult: " + totalPrice);
			}
		}
		}
		long endTime1 = System.currentTimeMillis();
		
		System.out.println("time: " + (endTime1 - startTime1));
		return true;
	}
}
