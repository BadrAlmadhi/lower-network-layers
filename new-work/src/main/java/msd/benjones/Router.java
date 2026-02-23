package msd.benjones;

import java.util.HashMap;

public class Router {

    private HashMap<Router, Integer> distances;
    private String name;
    public Router(String name) {
        this.distances = new HashMap<>();
        this.name = name;
    }

    public void onInit() throws InterruptedException {

		//TODO: IMPLEMENT ME
		//As soon as the network is online,
		//fill in your initial distance table and broadcast it to your neighbors

        // distance to itself  is 0.
        distances.put(this, 0);

        // if only my direct neighbors at startup
        for (Neighbor n : Network.getNeighbors(this)) {
            distances.put(n.router, n.cost);
        }

        // send my distance table to all neighbors start bellman_ford
        for (Neighbor n : Network.getNeighbors(this)) {
            Network.sendDistanceMessage(
                    new Message(this, n.router, new HashMap<>(distances))
            );
        }
    }

    public void onDistanceMessage(Message message) throws InterruptedException {
		//update your distance table and broadcast it to your neighbors if it changed

        // find cost to the sender to get direct link
        int costToSender = Integer.MAX_VALUE;
        for(Neighbor n : Network.getNeighbors(this)) {
            if (n.router == message.sender) {
                costToSender = n.cost;
                break;
            }
        }

        boolean changed = false;

        // Bellman-For
        for (Router dest : message.distances.keySet()) {
            int senderToDest = message.distances.get(dest);
            int candidate = costToSender + senderToDest;

            int current = distances.getOrDefault(dest, Integer.MAX_VALUE);
            // if foing through sender is cheaper, update
            if (candidate < current) {
                distances.put(dest,candidate);
                changed = true;
            }
        }
        // if table changed, notify neighbors
        if (changed) {
            for (Neighbor n : Network.getNeighbors(this)) {
                Network.sendDistanceMessage(
                        new Message(this, n.router, new HashMap<>(distances))
                );
            }
        }
    }


    public void dumpDistanceTable() {
        System.out.println("router: " + this);
        for(Router r : distances.keySet()){
            System.out.println("\t" + r + "\t" + distances.get(r));
        }
    }

    @Override
    public String toString(){
        return "Router: " + name;
    }
}
