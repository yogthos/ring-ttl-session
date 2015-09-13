(ns ring-ttl-session.core
  (:require [expiring-map.core :as em]
            [ring.middleware.session.store :refer [SessionStore]]))

(deftype TTLMemoryStore [cache]
  SessionStore
  (read-session [_ k]
    (when-let [session (get cache k)]
      (em/assoc! cache k session)
      session))
  (write-session [_ k data]
    (let [k (or k (str (java.util.UUID/randomUUID)))]
      (em/assoc! cache k data)
      k))
  (delete-session [_ k]
    (em/dissoc! cache k)
    nil))

(defn ttl-memory-store
  "Returns an implementation of SessionStore where sessions have a time-to-live given
  in seconds."
  [ttl & [opts]]
  (TTLMemoryStore. (em/expiring-map ttl (merge {:expiration-policy :access} opts))))
