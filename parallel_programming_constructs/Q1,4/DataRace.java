public class DataRace {
    public static void main(String[] args) throws InterruptedException {
        CompanyStocks stock = new CompanyStocks();

        Thread market = new Thread(new Market(stock));
        Thread trader = new Thread(new Trader(stock, 900.0, 1100.0));
        
        market.start();
        trader.start();
        market.join();
        trader.join();
    }
}

class Market implements Runnable {
    CompanyStocks stock;

    Market(CompanyStocks s) {
        this.stock = s;
    }

    public void run() {
        while(true) {
            stock.price_change();
        }
    }
}

class Trader implements Runnable {

    CompanyStocks stock;
    char type;
    Double ulimit;
    Double dlimit;

    Trader(CompanyStocks s, Double l, Double u) {
        this.stock = s;
        this.dlimit = l;
        this.ulimit = u;
    }
    
    public void run() {
        while(true) {
            Double p = stock.get_price();
            if(p < dlimit) {
                System.out.println("Take Long Position; Indicated at " + p + "; Asset bought at "+stock.get_price());
                System.out.println("Clear Short Position; Indicated at " + p + "; Asset bought at " + stock.get_price());
            }
            else if(p > ulimit) {
                System.out.println("Clear Long Position; Indicated at " + p + "; Asset sold at "+ stock.get_price());
                System.out.println("Take Short Position; Indicated at " + p + "; Asset sold at "+stock.get_price());
            }
        }
    }
}


class CompanyStocks {
    Double stock_price = 1000.0;
    Boolean move = false;

    public synchronized Double get_price() {
        return stock_price;
    }

    public synchronized void price_change() {
        Double fluc = Math.random();
        Double change = 100*fluc;

        if(stock_price >= 1250) move = false;
        else if(stock_price <= 750) move = true;

        if(!move) change = -change;

        stock_price = stock_price+change;
    }
}