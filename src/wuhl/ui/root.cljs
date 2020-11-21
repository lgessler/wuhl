(ns wuhl.ui.root
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom :refer [div]]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.ui-state-machines :as sm]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.ui.drawer :refer [ui-drawer Drawer]]
            [wuhl.ui.home :refer [Home]]
            [wuhl.ui.common :refer [loader]]
            ))


(defn app-bar
  [this]
  (mui/app-bar {:position "static"}
    (mui/toolbar {:variant "dense"}
      (mui/icon-button {:edge    "start"
                        :color   "inherit"
                        :onClick #(m/set-value! this :root/drawer-open? true)}
        (muic/menu {:fontSize "large"}))
      ((mui/styled-typography {:flex-grow 1}) {:variant "h5"} "Wuhl"))))

(defn drawer
  [this {:root/keys [drawer-open?]}]
  (ui-drawer
    (c/computed drawer {:open?   drawer-open?
                        :onClose #(m/set-value! this :root/drawer-open? false)})))

(dr/defrouter RootRouter
  [this {:keys [current-state route-factory route-props]}]
  {:router-targets      [Home]
   :always-render-body? false}
  (loader))

(def ui-root-router (c/factory RootRouter))

(defsc Root [this {:root/keys [router drawer drawer-open?] :as props}]
  {:ident         (fn [] [:component/id :root])
   :query         [{:root/router (c/get-query RootRouter)}
                   {:root/drawer (c/get-query Drawer)}
                   :root/drawer-open?]
   :initial-state (fn [_] {:root/router       (c/get-initial-state RootRouter)
                           :root/drawer       (c/get-initial-state Drawer)
                           :root/drawer-open? false})}
  (mui/theme-provider {:theme mui/default-theme}
    (dom/div
      (app-bar this)
      (ui-drawer
        (c/computed drawer {:open?   drawer-open?
                            :onClose #(m/set-value! this :root/drawer-open? false)}))
      (ui-root-router router))))

(def ui-root (c/factory Root))

(defsc FulcroRoot [this {:keys [root]}]
  {:query         [{:root (c/get-query Root)}]
   :initial-state (fn [_] {:root (c/get-initial-state Root)})}
  ^:inline (ui-root root))