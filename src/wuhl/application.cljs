(ns wuhl.application
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txp]
    [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
    [com.fulcrologic.fulcro.mutations :as m]))

(defonce SPA
         (stx/with-synchronous-transactions
           (app/fulcro-app
             {:remotes {}})))

