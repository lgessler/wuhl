(ns wuhl.ui.drawer
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.ui-state-machines :as sm]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.material-ui :as mui]
            [wuhl.router :as r]
            [wuhl.material-ui-icon :as muic]))

(def ident [:component/id :drawer])

(defn drawer-item
  ([path text icon onClose]
   (drawer-item path text icon onClose nil))
  ([path text icon onClose divider]
   (mui/list-item {:key     text
                   :button  true
                   :divider (boolean divider)
                   :onClick (fn [e]
                              (onClose)
                              (r/route-to! path))}
                  (mui/list-item-icon {} (icon))
                  (mui/list-item-text {} text))))

(defsc Drawer [this props {:keys [onClose open?]}]
  {:query         []
   :ident         (fn [] ident)
   :initial-state {}}
  (let [styled-list (mui/styled-list {:width 300})]
    (mui/drawer
      {:open    open?
       :onClose onClose
       :anchor  "left"}
      (styled-list {}
        (drawer-item :home "Home" muic/home onClose)))))

(def ui-drawer (c/factory Drawer))
