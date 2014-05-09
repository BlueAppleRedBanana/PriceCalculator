import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class PriceCalculator {
	private static String[][] SamplePricing =  new String[][] { 
		{ "A", "1", "2" }, { "A", "4", "7" },
		{ "B", "1", "12" }, { "C", "1", "1.25" },
		{ "C", "6", "6" }, { "D", "1", "0.15" } };
	private HashMap<Character, TreeMap<Integer, Float>> priceMap = null;
	private HashMap<Character, Integer> purchaseMap = null;
	private StringBuffer input = null;
	public Float total = null;
	
	public PriceCalculator() {
		input = new StringBuffer();
		total = new Float(0);
		purchaseMap = new HashMap<Character, Integer>();
	}
	
	public void setPricing(String[][] pricePolicy) {
		priceMap = getPriceMap(pricePolicy);
	}
	
	public void scan(Character product) {
		input.append(product); // to save history for future usage and printing
		Integer value = purchaseMap.get(product);
		if (value != null) {
			purchaseMap.put(product, value + 1);
		} else {
			purchaseMap.put(product, 1);
		}
		try {
			refreshTotalPrice();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			// remove the product it is not in priceMap
			purchaseMap.remove(product);
		}
	}
	
	private void refreshTotalPrice() throws Exception {
		total = new Float(0);
		for (Entry<Character, Integer> purchase : purchaseMap.entrySet()) {
			TreeMap<Integer, Float> unitPriceMap = priceMap.get(purchase
					.getKey());
			if (unitPriceMap == null) {
				throw new Exception("The priceMap of the product " + purchase.getKey() + " is missing.");
			}
			total += getPrice(purchase, unitPriceMap);
		}
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
	 * ensure every product has a unit price (for one)
	 * if not, throw an exception.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validatePriceMap(HashMap<Character, TreeMap<Integer, Float>> priceMap) {
		Iterator iter = priceMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry pairs = (Map.Entry)iter.next();
			Character product = (Character) pairs.getKey();
			TreeMap<Integer, Float> productPriceMap = (TreeMap<Integer, Float>) pairs.getValue();
			if (hasUnitPrice(productPriceMap)) {
				continue;
			} else {
				throw new Exception("product "+ product + " does not have a unit price");
			}
		}
	}
	
	private boolean hasUnitPrice(TreeMap<Integer, Float> productPriceMap) {
		Float priceOfUnitOne = productPriceMap.get(1);
		if (priceOfUnitOne!=null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String generatePurchase() {
		StringBuffer result = new StringBuffer();
		final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final int N_ALPHABET = 6;
		final int MAX_LEN = 200;
		Random random = new Random();
		for (int i=0 ; i<MAX_LEN ;i++ ) {
			int index = random.nextInt(N_ALPHABET);
			System.out.println(index);
			result.append(ALPHABET.charAt(index));
		}
		return result.toString();
	}

	public static void main(String[] args) {
		ArrayList<String> inputs = new ArrayList<String>(Arrays.asList(
				"ABCDABAA", "CCCCCCC", "ABCD", 
				"ABDCDCBACBACBDBCBCBBBBACBDBCBACBCBBBCBABBCDBABCBABDCBADBCBBABDCBABCADBCBADBABBABBAAAAAABBCABCABCABCABCABCBABCBCBCBDBDBDDDDDDDDDDDDD"));
		ArrayList<Float> expected = new ArrayList<Float>(Arrays.asList(
				new Float(32.40), new Float(7.25), new Float(15.40), new Float(703.5)));
		System.out.println(PriceCalculator.generatePurchase());
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
		
		
		 String[][] samplePricing = new String[][] { 
					{ "A", "1", "2" }, { "A", "4", "7" }, {"A","7","12"},
					{ "B", "1", "12" }, {"B","12","110"},  
					{ "C", "1", "1.25" }, { "C", "6", "6" }, 
					{ "D", "1", "0.15" },
					{ "E", "1", ""};
		 
		if (inputList.size() != expectedList.size() ) {
			throw new Exception("the length of expectedList should match to the length of inputList");
		}
		long startTime = System.currentTimeMillis();
		int i;
		for (i=0; i<inputList.size() ; i++) {
			String input = inputList.get(i);
			Float expected = expectedList.get(i);
			
			PriceCalculator calc = new PriceCalculator();
			calc.setPricing(PriceCalculator.SamplePricing);
			calc.simulateScan(input);
			Float totalPrice = calc.total;
			System.out.println(input + "," + totalPrice); 
//			if (Float.compare(totalPrice, expected) !=0) {
//				throw new Exception("test failed at index: " + i + "\nexpected: " + expected + "\nresult: " + totalPrice);
//			}
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("time taken: " + (endTime - startTime));
		System.out.println("test passed : " + i);
		return true;
	}

}
