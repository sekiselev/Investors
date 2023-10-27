package ru.seliselev.investor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.seliselev.investor.entity.Transaction;
import ru.seliselev.investor.entity.User;
import ru.seliselev.investor.enums.TransactionType;
import ru.seliselev.investor.logging.MyLogger;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.seliselev.investor.enums.TransactionType.BUY;

@Service
@Slf4j
public class StockService {
    private static final String URL_GET_STOCK_PRICE = "https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/";
    private static final Logger logger = LoggerFactory.getLogger(MyLogger.class);

    public StockService() throws IOException {
    }

    @Autowired
    TransactionService transactionService;
    @Autowired
    UserService userService;

    public static BigDecimal getStockPrice(String ticker) {
        try {
            // Создаем экземпляр HttpClient
            HttpClient httpClient = HttpClients.createDefault();

            // URL для запроса цены акции
            String url = URL_GET_STOCK_PRICE + ticker + ".json";

            // Создаем GET-запрос
            HttpGet httpGet = new HttpGet(url);

            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем, что код ответа успешный (200 OK)
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа
                String jsonResponse = EntityUtils.toString(response.getEntity());

                // Разбираем JSON-ответ
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray data = jsonObject.getJSONObject("securities").getJSONArray("data");
                double price = data.getJSONArray(0).getDouble(3); // Индекс 3 содержит цену акции

                return BigDecimal.valueOf(price);
            } else {
                // Обработка ошибки, если код ответа не 200 OK
                System.err.println("Ошибка при выполнении запроса: " + response.getStatusLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.valueOf(0); // Возвращаем 0 в случае ошибки
    }

    // удаляет из коллекции название акции количество, все акции с количеством равным нулю
    public static Map<String, Integer> removeZeroEntries(Map<String, Integer> map) {
        map.entrySet().removeIf(entry -> entry.getValue().equals(0));
        return map;
    }

    /**
     * получает полное название акции по стикеру,
     * используя парсинг файла stocks.json в паке resources
     */

    public static String getStockName(String ticker) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode dictionary = mapper.readTree(new ClassPathResource("stocks.json").getContentAsByteArray());

        JsonNode nameNode = dictionary.get(ticker);

        if (nameNode == null) {
            return null;
        }

        return nameNode.asText();

    }

    /**
     * Возвращает список акций пользователя
     * в виде map: <название акции - количество>
     */
    public Map<String, Integer> getUserStocks(User user) {
        Map<String, Integer> usersStocksMap = user.getTransactions()
                .stream()
                .collect(Collectors.groupingBy(transaction -> {
                            try {
                                return StockService.getStockName(transaction.getStock().getName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Collectors.summingInt(transaction -> transaction.getType() == TransactionType.SELL ? -transaction.getQuantity() : transaction.getQuantity())));
        //удаляем акции  из коллекции с количеством равным нулю
        return removeZeroEntries(usersStocksMap);
    }

    public Map<String, Integer> getUserStocks_(User user) {
        Map<String, Integer> usersStocksMap = user.getTransactions()
                .stream()
                .collect(Collectors.groupingBy(transaction -> {
                            return transaction.getStock().getName();
                        },
                        Collectors.summingInt(transaction -> transaction.getType() == TransactionType.SELL ? -transaction.getQuantity() : transaction.getQuantity())));
        //удаляем акции  из коллекции с количеством равным нулю
        return removeZeroEntries(usersStocksMap);
    }

    /**
     * Возвращает список акций пользователя
     * в виде map: <название акции - сумма покупки> в разрезе названия акции
     */
    public Map<String, BigDecimal> getUserStocksAmount(User user) {
        return user.getTransactions()
                .stream()
                .collect(Collectors.groupingBy(
                        transaction -> {
                            try {
                                return StockService.getStockName(transaction.getStock().getName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                transactions -> {
                                    // Берем последнюю покупку
                                    Transaction lastBuy = transactions.stream()
                                            .filter(t -> t.getType() == BUY)
                                            .sorted(Comparator.comparing(Transaction::getDate).reversed())
                                            .findFirst()
                                            .orElse(null);

                                    if (lastBuy != null) {
                                        // Возвращаем стоимость по цене последней покупки
                                        return lastBuy.getPurchasePrice()
                                                .multiply(BigDecimal.valueOf(transactions.stream()
                                                        .mapToInt(t -> t.getType() == BUY ? t.getQuantity() : -t.getQuantity())
                                                        .sum()));
                                    }

                                    return BigDecimal.ZERO;
                                }
                        )
                ));
    }


    /**
     * Формирует атрибуты модели на основе данных пользователя используется для двух контроллеров
     * по запросу цены акции и по покупке или продаже акций
     */
//
    public void prepareModelAttributes(Model model, User user, String stockName) throws IOException {
        model.addAttribute("user", user);

        Map<String, Integer> userStocks = getUserStocks(user);
        model.addAttribute("userStocks", userStocks);

        Map<String, BigDecimal> userStocksSum = getUserStocksAmount(user);
        model.addAttribute("userStockSum", userStocksSum);
        model.addAttribute("userBalance", user.getAccount().getBalance());

        if (stockName != null) {
            model.addAttribute("stockPrice", getStockPrice(stockName));
            model.addAttribute("stockName", getStockName(stockName));
        }

        Map<String, Integer> userStocks_ = getUserStocks_(user);
        Map<String, BigDecimal> profitLossMap = new HashMap<>();

        for (String ticker : userStocks_.keySet()) {
            int quantity = userStocks_.get(ticker);
            List<Transaction> sortedTransactions = user.getTransactions()
                    .stream()
                    .filter(t -> t.getStock().getName().equals(ticker) && t.getType() == BUY)
                    .sorted(Comparator.comparing(Transaction::getDate))
                    .collect(Collectors.toList());

            BigDecimal purchaseTotal = BigDecimal.ZERO;
            int remainingShares = quantity;

            for (Transaction transaction : sortedTransactions) {
                if (transaction.getQuantity() <= remainingShares) {
                    purchaseTotal = purchaseTotal.add(transaction.getPurchasePrice().divide(new BigDecimal(transaction.getQuantity()), RoundingMode.HALF_UP).multiply(new BigDecimal(transaction.getQuantity())));
                    remainingShares -= transaction.getQuantity();
                } else {
                    purchaseTotal = purchaseTotal.add(transaction.getPurchasePrice().divide(new BigDecimal(transaction.getQuantity()), RoundingMode.HALF_UP).multiply(new BigDecimal(remainingShares)));
                    break;
                }
            }

            BigDecimal profitLoss = calculateProfitLoss(ticker, quantity, purchaseTotal);
            profitLossMap.put(getStockName(ticker), profitLoss);
        }

        model.addAttribute("profitLossMap", profitLossMap);
    }

    /**
     * расчитываем доход или расход по каждой купленной акции пользователя
     */
//    public BigDecimal calculateProfitLoss(String ticker, int quantity, BigDecimal purchasePrice) {
//
//        BigDecimal currentPrice = getStockPrice(ticker);
//        logger.info("текущая цена акции  "+ ticker+" равна "+currentPrice);
//
//        BigDecimal purchaseTotal = purchasePrice.multiply(BigDecimal.valueOf(quantity));
//         logger.info("стоимость акций   "+ ticker+ "в количестве "+ quantity +" равна "+purchasePrice);
//
//        BigDecimal currentTotal = currentPrice.multiply(BigDecimal.valueOf(quantity));
//
//       logger.info("по текущей цене акции стоят   "+ ticker+" равна "+currentTotal);
//
//        return currentTotal.subtract(purchaseTotal);
//
//    }

    public BigDecimal calculateProfitLoss(String ticker, int quantity, BigDecimal purchasePrice) {

        BigDecimal currentPrice = getStockPrice(ticker);
        logger.info("текущая цена акции  "+ ticker+" равна "+currentPrice);

        BigDecimal purchaseTotal = purchasePrice.multiply(BigDecimal.valueOf(quantity));
        logger.info("стоимость акций   "+ ticker+ "в количестве "+ quantity +" равна "+purchaseTotal);

        BigDecimal currentTotal = currentPrice.multiply(BigDecimal.valueOf(quantity));

        logger.info("по текущей цене акции стоят   "+ ticker+" равна "+currentTotal);

        return currentTotal.subtract(purchaseTotal);
    }

}
