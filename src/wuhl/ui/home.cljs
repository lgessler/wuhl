(ns wuhl.ui.home
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.router :as r]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]))

(def ident [:component/id :home])

(defsc Home [this {:keys [message] :as props}]
  {:query         [:message]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:message "Hello, world!"})
   :route-segment (r/last-route-segment :home)}
  (mui/container {}
    (mui/typography {} message)
    ))

