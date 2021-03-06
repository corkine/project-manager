(ns icemanager.about
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [icemanager.modals :as modals]
            [goog.string :as gstring]
            [clojure.string :as string]))

(def version "beta 1.0.1")

(def log (str "now: " version "
[0000-00-00]
这里是开发日志。

================================================
愿望清单：
- 这里是愿望清单。
"))

(defn about-page []
  [:div.hero.is-danger.is-fullheight-with-navbar
   [:section.section>div.container>div.content
    [:p.title "由 Corkine Ma 开发"]
    (let [usage @(rf/subscribe [:usage])
          server-back @(rf/subscribe [:wishlist-server-back])
          wish-list @(rf/subscribe [:wishlist])
          real-wish-list (filter #(= (:kind %) "愿望") wish-list)
          bug-list (filter #(= (:kind %) "BUG") wish-list)]
      [:<>
       [:p {:style {:margin-top :-20px}} (str "本服务已服务 " (:pv usage) " 人，共计 " (:uv usage) " 次")]
       [:pre (str log
                  "\n================================================\n数据库记录的请求：\n"
                  (string/join "\n"
                               (map (fn [line] (str "- " (:advice line)
                                                    " / 来自：" (:client line) "")) real-wish-list))
                  "\n\n================================================\n数据库记录的 BUG：\n"
                  (string/join "\n"
                               (map (fn [line] (str "- " (:advice line)
                                                    " / 来自：" (:client line) "")) bug-list))
                  "\n\n================================================\n最近 10 次 API 更改：\n"
                  (string/join "\n"
                               (map #(gstring/format "%-15s %-4s %-20s %-s"
                                                     (:from %) (string/upper-case (:method %))
                                                     (:api %) (:time %))
                                    (:usage usage))))]
       [:div.mb-3
        (r/with-let
          [user (r/atom nil)
           kind (r/atom "愿望")
           advice (r/atom nil)
           error (r/atom nil)]
          [modals/modal-button :wishlist
           {:button {:class ["is-light" "mt-0"]}}
           (str "提愿望/建议/BUG")
           [:div {:style {:color "black"}}
            [:<>
             [:label.label {:for "user"} "称呼 *"]
             [:input.input {:type        :text
                            :id          :user
                            :value       (or @user "")
                            :placeholder "输入你的称呼"
                            :on-change   #(reset! user (.. % -target -value))}]
             [:label.label.mt-4 {:for "kind"} "类别 *"]
             [:div.select>select {:id        :kind
                                  :value     @kind
                                  :on-change #(reset! kind (.. % -target -value))}
              [:option {:value "愿望"} "愿望"]
              [:option {:value "建议"} "建议"]
              [:option {:value "BUG"} "BUG"]]
             [:label.label.mt-4 {:for "advice"} (str @kind " * (不少于 10 个字)")]
             [:textarea.textarea {:rows        4
                                  :value       (or @advice "")
                                  :id          :advice
                                  :on-change   #(reset! advice (.. % -target -value))
                                  :placeholder (str "输入你的" @kind)}]
             (when server-back
               [(if (= (:status server-back) :success)
                  :div.notification.is-success.mt-4
                  :div.notification.is-danger.mt-4) (str (:content server-back))])
             (when-let [message @error]
               [:div.notification.is-warning.mt-4 message])]]
           (let [is-success-call (and (not (nil? server-back))
                                      (= (:status server-back) :success))]
             [:button.button.is-primary.is-fullwidth
              {:on-click (if is-success-call
                           (fn [_]
                             (reset! error nil)
                             (reset! user nil)
                             (reset! kind "愿望")
                             (reset! advice nil)
                             (rf/dispatch [:clean-wishlist-server-back])
                             (rf/dispatch [:app/hide-modal :wishlist]))
                           (fn [_]
                             (reset! error nil)
                             (cond (nil? @user) (reset! error "称呼不能为空")
                                   (nil? @kind) (reset! error "类别不能为空")
                                   (nil? @advice) (reset! error (str @kind "不能为空"))
                                   (< (count @advice) 10) (reset! error (str @kind "少于 10 个字。"))
                                   :else (rf/dispatch [:send-wishlist {:client @user
                                                                       :kind   @kind
                                                                       :advice @advice}]))))}
              (if is-success-call "关闭" "提交")])])]])
    [:pre "Powered by clojure & clojureScript.
Build with shadow-cljs, cljs-ajax, reagent, re-frame, react, bulma, http-kit, muuntaja, swagger, ring, mount, conman, cprop, cheshire, selmer, google closure compiler.
Managed by lein, maven and npm.
Data stored with postgreSQL.
Developed with firefox and IDEA.
All Open Source Software, no evil."]]])