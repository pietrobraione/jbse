<script>
(function () {

  function LoadMathJax() {
    if (!window.MathJax) {
      if (document.body.innerHTML.match(/$|\\\[|\\\(|<([a-z]+:)math/)) {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "https://c328740.ssl.cf1.rackcdn.com/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML";
        script.text = [
          "MathJax.Hub.Config({",
          "  tex2jax: {inlineMath: [['$','$'],['\\\\\(','\\\\\)']]}",
          "});"
        ].join("\n");
        var parent = (document.head || document.body || document.documentElement);
        parent.appendChild(script);
      }
    }
  };

  var script = document.createElement("script");
  script.type = "text/javascript";
  script.text = "(" + LoadMathJax + ")()";
  var parent = (document.head || document.body || document.documentElement);
  setTimeout(function () {
    parent.appendChild(script);
    parent.removeChild(script);
  },0);

})();
</script>

JBSE
====

Introduction
------------

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. If you are not confident about what "symbolic execution" means, please read the corresponding [Wikipedia article](http://en.wikipedia.org/wiki/Symbolic_execution). But if you are really impatient, symbolic execution is to testing what symbolic equation solving is to numeric equation solving: A numeric equation `\(x^2 - 2x + 1\)`
