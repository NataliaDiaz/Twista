var timeplot;

function onLoad() {
  var plotInfo = [
    Timeplot.createPlotInfo({
      id: "plot1"
    })
  ];
            
  timeplot = Timeplot.create(document.getElementById("sentimentTimeplot"), plotInfo);
}

var resizeTimerID = null;
function onResize() {
    if (resizeTimerID == null) {
        resizeTimerID = window.setTimeout(function() {
            resizeTimerID = null;
            timeplot.repaint();
        }, 100);
    }
}