(ns wuhl.ui.common
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as c]
            [wuhl.material-ui :as mui]
            [wuhl.i18n :refer [tr]]))

(defn loader []
  (mui/box {:alignItems     "center"
            :justifyContent "center"
            :display        "flex"
            :minHeight      400}
    (mui/circular-progress {:size "6em"})))

