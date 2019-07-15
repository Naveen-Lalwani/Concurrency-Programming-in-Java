/**
 * @author Naveen Lalwani
 */
package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;
/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor();
        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });
        
        int totalPrimeNumbers = 1;
        SieveActorActor temp = sieveActor;
        while (temp != null) {
            totalPrimeNumbers += temp.numLocalPrimes;
            temp = temp.nextActor;
        }
        return totalPrimeNumbers;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        static final int MAX_LOCAL_PRIMES = 1000;
        int[] localPrimes = new int[MAX_LOCAL_PRIMES];
        SieveActorActor nextActor = null;
        int numLocalPrimes = 0;
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;

            // Special message indicating termination.
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
                return;
            }
            
            // If not locally prime, ignore.
            if (!isLocallyPrime(candidate)) {
                return;
            }

            if (numLocalPrimes < MAX_LOCAL_PRIMES) {
                localPrimes[numLocalPrimes] = candidate;
                numLocalPrimes += 1;
                return;
            }
            
            if (nextActor == null) {
                nextActor = new SieveActorActor();
            }
            
            nextActor.send(msg);
        }

        /**
         * Method to check if the number is locally prime.
         * @param candidate Number to be checked.
         * @return True if prime else false.
         */
        public boolean isLocallyPrime(int candidate) {
            for (int i = 0; i < numLocalPrimes; i++) {
                if (candidate % localPrimes[i] == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
