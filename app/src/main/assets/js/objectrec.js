var targetName;

var World = {
    loaded: false,
    drawables: [],

    createEverything: function createEverythingFn() {

        hideInfoBarFn();
        // load wto file containing the 3d models
        this.targetCollectionResource = new AR.TargetCollectionResource("assets/tracker.wto", {});

        this.tracker = new AR.ObjectTracker(this.targetCollectionResource, {
            maximumNumberOfConcurrentlyTrackableTargets: 2, // a maximum of 5 targets can be tracked simultaneously
            extendedRangeRecognition: AR.CONST.IMAGE_RECOGNITION_RANGE_EXTENSION.OFF,
            onTargetsLoaded : ()=>{},
            onError         : (errorMessage)=>{ alert(errorMessage)}
        });

        // function to hide the info bar
        hideInfoBar: function hideInfoBarFn() {
                document.getElementById("infoBox").style.display = "none";
                document.getElementById("loadingMessage").style.display = "table";
        }

        // function to show the info bar
        showInfoBar: function worldLoadedFn(targetName) {
                document.getElementById("infoBox").style.display = "table";
                document.getElementById("loadingMessage").style.display = "none";
                document.getElementById("foundinfo").innerHTML = "Object Found: " + targetName
        }

        this.objectTrackable = new AR.ObjectTrackable(this.tracker, "*", {
            // function called when object recognised
            onObjectRecognized: function(targetName) {

                 worldLoadedFn(targetName);
                 World.createLabels(targetName);
                 var scanbutton = document.getElementById("scanbutton");
                 // show 'Add' button
                 scanbutton.style.visibility = "visible";
                 targetName1 = targetName;
                 function sendName(){changeActivity()};
            },
            onObjectLost: function(){World.removeLabels(); hideInfoBarFn();},
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });
    },
    createLabels: function createLabelsFn(targetName){
        if (targetName == "Dinosaur") {
            var titleLabel = new AR.Label(targetName, 0.5, { rotate : { y: 180 }, translate: { y: 1.5 }, style: { textColor: '#FFFFFF', fontStyle: AR.CONST.FONT_STYLE.BOLD } });
         }
         else if (targetName == "Aircraft") {
            var titleLabel = new AR.Label(targetName, 0.5, { rotate : { y: 180 }, translate: { y: 1.5, x: 0.5 }, style: { textColor: '#FFFFFF', fontStyle: AR.CONST.FONT_STYLE.BOLD } });
         }
         else {
             var titleLabel = new AR.Label(targetName, 0.5, { rotate : { y: -40 }, translate: { y: 2, x: -1.5 }, style: { textColor: '#FFFFFF', fontStyle: AR.CONST.FONT_STYLE.BOLD } });
         }
            // show the 3d label of the recognised object when in the field of view
            World.allCurrentModels = [];
            World.allCurrentModels = World.allCurrentModels.concat([titleLabel]);
            World.objectTrackable.drawables.addCamDrawable(World.allCurrentModels);
        },
        // remove the 3d label of the recognised object when not in the field of view
        removeLabels: function removeLabelsFn(){
            World.objectTrackable.drawables.removeCamDrawable(0);
        }
};

World.createEverything();

// function that send the name of the recognised object
function changeActivity(){AR.platform.sendJSONObject({targetName1})};