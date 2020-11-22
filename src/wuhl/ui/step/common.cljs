(ns wuhl.ui.step.common
  (:require [wuhl.material-ui :as mui]
            [wuhl.i18n :refer [tr]]
            [com.fulcrologic.fulcro.components :as c]))

(defn next-button [props]
  (mui/button (merge {:variant "contained"
                      :color   "primary"}
                     props)
    (tr "Next Step")))

(defn back-button [props]
  (mui/button (merge {:variant "outlined"}
                     props)
    (tr "Back")))

(defn reset-button [props]
  (mui/button (merge {:variant "outlined"}
                     props)
    (mui/typography {:variant "button"
                     :color   "textSecondary"}
      (tr "Reset"))))

(defn dummy-button []
  (mui/button {:variant "outlined" :disabled true} " "))

(defn action-div [{:keys [back next reset]}]
  (mui/grid {:container true :direction "row" :spacing 1 :style {:marginTop "2em"}}
    (mui/grid {:item true :sm 1}
      (when reset
        reset))
    (mui/grid {:item true :sm 5})
    (mui/grid {:item true :sm 6}
      (when (or back next)
        (mui/button-group {:fullWidth true}
          (or back (dummy-button))
          (or next (dummy-button)))
        )
      )))
