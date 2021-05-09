import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Transaction{
	private LocalTime time;
	private String    shopName;
	private LocalDate date;
	private Integer   price;
	private boolean validContent;

	private String contentRegxp = "(?<=お支払い\\s)[\\d,]{1,8}(?=\\s円)";
	private String shopRegxep   = "(?<=加盟店:\\s).*(?=\\n)";
	private Pattern  contentPattern = Pattern.compile(contentRegxp);
	private Pattern  shopPattern     = Pattern.compile(shopRegxep);

	@Override
	public String toString() {
		return "Date: " + this.date + " Time: " + this.time + " Price: " + this.price + " shopName: " + this.shopName;
	}

	Transaction(LocalDate date, String timeString, String contents){

		this.date = date;
		this.time = LocalTime.of(Integer.valueOf(timeString.split(":")[0]), Integer.valueOf(timeString.split(":")[1]));
		Matcher matcher = contentPattern.matcher(contents);
		this.price = matcher.find() ? Integer.valueOf( matcher.group().replaceAll(",", "")) : null;

		Matcher matcher2 = shopPattern.matcher(contents);
		this.shopName    = matcher2.find() ? matcher2.group() : null;
		this.validContent = this.price != null && this.shopName != null;
	}

	public boolean isValidContent() {
		return validContent;
	}

}


class TransactionHistoryParser{
  public static void main(String[] args){
	List<String> history = null;

    Path file = Paths.get("C:\\Users\\broad\\OneDrive\\products\\[LINE] LINEウォレットとのトーク.txt");
    Map<LocalDate, String> mapedByDate = new LinkedHashMap<LocalDate, String>();
    String dateRegexp = "[0-9]{4}\\/[0-9]{1,2}\\/[0-9]{1,2}\\([月火水木金土日]\\)";
    Pattern dateString = Pattern.compile(dateRegexp);
    List<Transaction> transactions = new ArrayList<>();


	try {
		String result = Files.readAllLines(file).stream().reduce("" ,(a,b) -> a + "\n" +  b);

		List<LocalDate> parsedDates = new ArrayList<>();
		Matcher matcher = dateString.matcher(result);

		while(matcher.find()) {
			String matched = matcher.group();
			int year  = Integer.valueOf(matched.split("\\/")[0]);
			int month = Integer.valueOf(matched.split("\\/")[1]);
			int day   = Integer.valueOf(matched.split("\\/")[2].replaceFirst("\\([月火水木金土日]\\)", ""));

			parsedDates.add(LocalDate.of(year,month,day));
		}

		List<String> contents = Arrays.asList(result.split(dateRegexp));
		for(int i = 0; i < contents.size()-1; i++) {
			mapedByDate.put(parsedDates.get(i), contents.get(i+1));
		}

		for(Map.Entry<LocalDate,String> pair:mapedByDate.entrySet()) {


			String transactionString = pair.getValue();
			String timeRegxep  = "[0-9]{1,2}:[0-9][0-9]";
			Pattern timeString = Pattern.compile(timeRegxep);
			Matcher timeMatcher = timeString.matcher(pair.getValue());

			List<String> matchedTimes = new ArrayList<>();

			while(timeMatcher.find()) {
				String matched2 = timeMatcher.group();
				matchedTimes.add(matched2);
			}

			List<String> transactionContents = Arrays.asList(pair.getValue().split(timeRegxep));

			for(int i = 0; i < transactionContents.size()-1; i++) {
				transactions.add(new Transaction(pair.getKey(), matchedTimes.get(i), transactionContents.get(i+1)));
			}

		}

	} catch (IOException e) {
		e.printStackTrace();
	}

	transactions.stream().filter(x -> x.isValidContent()).forEach(System.out::println);
	System.out.println("Invalid Date Count: " + (transactions.stream().filter(x -> !x.isValidContent()).count()));

	//history.stream().forEach(System.out::println);

  }
}