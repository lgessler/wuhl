(ns wuhl.i18n
  (:require [taoensso.tempura :as tempura]))

;; any translatable text should be passed through here
;; we'll use invocations of this function later if we
;; implement translations
(def tr identity)
(def lipsum "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.")