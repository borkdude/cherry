[Cherry](https://github.com/squint-cljs/cherry): Experimental ClojureScript to ES6 module compiler

- [#119](https://github.com/squint-cljs/cherry/issues/119): Add a `bb` task for serving the playground

## 0.1.13 (2023-12-03)

- Fix varargs, allow `Math` to be used unqualified

## 0.1.12 (2023-11-30)

- Alas, the previous build didn't pick up the added vars

## 0.1.11 (2023-11-30)

- Add Clojure 1.11 functions like `parse-long`, etc (notice how the version numbers align!)

## 0.1.10 (2023-11-30)

- Catch up with newest squint common compiler enhancements
- Support `js-await` (rather than `js/await`)
- [Update playground](https://squint-cljs.github.io/cherry/?src=https://gist.githubusercontent.com/borkdude/ca3af924dc2526f00361f28dcf5d0bfb/raw/09cd9e17bf0d6fa3655d0e7cbf2c878e19cb894f/pinball.cljs)!

## 0.1.9 (2023-10-04)

- Fix reading `:cljs` branches from `.cljc` files

## 0.1.8 (2023-09-18)

- Bump shared compiler code with squint, optimizes truthiness checks and fixes `and` and `or` wrt/ truthiness

## 0.1.7 (2023-08-18)

- Fix overriding core vars

## 0.1.6 (2023-08-18)

- Add `clojure.string` and `clojure.walk`

## 0.1.5 (2023-08-15)

- Support `defclass` and `js-template`, same as squint. See [squint docs](https://github.com/squint-cljs/squint/blob/main/doc/defclass.md).

## 0.0.4 (2023-06-03)

- [#101](https://github.com/squint-cljs/cherry/issues/101): include `lib/cherry.umd.js` fallback for Firefox web worker

## 0.0.3 (2023-05-13)

- Adjust core package in standard import

## 0.0.2 (2023-05-13)

- [#93](https://github.com/squint-cljs/cherry/issues/93): optimize `if` + `zero?`
- [#98](https://github.com/squint-cljs/cherry/issues/98): fix `$default` import

## 0.0.1

- Support `cherry.embed` namespace to embed cherry into a CLJS/shadow app. See [docs](doc/embed.md).
- Many other small fixes

## 0.0.0-alpha.60

- [#78](https://github.com/squint-cljs/cherry/issues/78): fix macro call with more than 20 arguments
- [#79](https://github.com/squint-cljs/cherry/issues/79): fix issue with advanced compilation and `_EQ_` symbol

## 0.0.0-alpha.59

- [#78](https://github.com/squint-cljs/cherry/issues/78): fix macro call with more than 20 arguments
- [#79](https://github.com/squint-cljs/cherry/issues/79): fix (workaround) issue with advanced compilation and `_EQ_` symbol

## 0.0.0-alpha.58

- [#71](https://github.com/squint-cljs/cherry/issues/71): support alias with dashes
- [#77](https://github.com/squint-cljs/cherry/issues/77): support async/await in variadic function
s
- [#67](https://github.com/squint-cljs/cherry/issues/67): support namespaced components in JSX

## 0.0.0-alpha.57

- Fix rendering of number attributes in JSX
