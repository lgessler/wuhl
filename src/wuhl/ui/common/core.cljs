(ns wuhl.ui.common.core
  (:require [wuhl.material-ui :as mui]))

(defn loader []
  (mui/box {:alignItems     "center"
            :justifyContent "center"
            :display        "flex"
            :minHeight      400}
           (mui/circular-progress {:size "6em"})))
