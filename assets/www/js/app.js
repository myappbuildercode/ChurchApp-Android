// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'starter.controllers' is found in controllers.js







var app=angular.module('starter', ['ionic', 'starter.controllers', 'ui.tinymce', 'angularPayments'])

app.run(function($ionicPlatform, $ionicPopup, $interval, $rootScope, $location, $ionicLoading) {
			
			$ionicPlatform.registerBackButtonAction(function () {
			
                                            navigator.notification.confirm('This will exit from the app, Do you want to continue?', function (button) {
                                            if (button == 1) {
          									ionic.Platform.exitApp();
                                            }else{
                                            }
                                            },'Church App',["OK","CANCEL"]);
                                            
                                            }, 100);
                                            
                                            
                                            
  document.addEventListener("offline", onOffline, false);

    function onOffline() {
 
           var alertPopup = $ionicPopup.alert({
     	   title: 'Network Failure',
     	   template: 'Please Check Your Internet Connection'
   									});
   		   alertPopup.then(function(res) {
     
   									});    
        }
                                                           
                                                      
                                                           
	})




app.config(function($stateProvider, $urlRouterProvider, $ionicConfigProvider) {
  $ionicConfigProvider.views.maxCache(0);
    $ionicConfigProvider.views.transition('none');
    $ionicConfigProvider.navBar.alignTitle("center");
  $stateProvider.state('login', {
    url: "/login",
    templateUrl: "templates/login.html",
    controller: 'LoginCtrl'
  })
  $stateProvider.state('home', {
    url: "/home",
    templateUrl: "templates/home.html",
    controller: 'homeCtrl'
            })
   $stateProvider.state('sliderpagecreate', {
     url: "/sliderpagecreate",
     templateUrl: "templates/sliderpagecreate.html",
     controller: 'sliderpagecreateCtrl'
                                })
           
   $stateProvider.state('imageandtextcreate', {
      url: "/mageandtextcreate",
      templateUrl: "templates/imageandtextcreate.html",
      controller: 'imageandtextcreateCtrl'
                                })
   $stateProvider.state('imageandtextpage', {
        url: "/imageandtextpage",
        templateUrl: "templates/imageandtextpage.html",
        controller: 'imageandtextpageCtrl'
                                })
   $stateProvider.state('sliderpageedit', {
         url: "/sliderpageedit",
         templateUrl: "templates/sliderpageedit.html",
         controller: 'sliderpageeditCtrl'
                                })
  $stateProvider.state('imageandtextpageedit', {
         url: "/imageandtextpageedit",
         templateUrl: "templates/imageandtextpageedit.html",
         controller: 'imageandtextpageeditCtrl'
                                })
  $stateProvider.state('textlistpagecreate', {
          url: "/textlistpagecreate",
          templateUrl: "templates/textlistpagecreate.html",
          controller: 'textlistpagecreateCtrl'
                                })
  $stateProvider.state('textlistpage', {
           url: "/textlistpage",
           templateUrl: "templates/textlistpage.html",
           controller: 'textlistpageCtrl'
                                })
  $stateProvider.state('textlistaddpage', {
          url: "/textlistaddpage",
          templateUrl: "templates/textlistaddpage.html",
          controller: 'textlistaddpageCtrl'
                                })
                                
   $stateProvider.state('textlisteditpage', {
           url: "/textlisteditpage",
           templateUrl: "templates/textlisteditpage.html",
           controller: 'textlisteditpageCtrl'
                                })
                                
    $stateProvider.state('programeventdetailspage', {
           url: "/programeventdetailspage",
           templateUrl: "templates/programeventdetailspage.html",
           controller: 'programeventdetailspageCtrl'
                                })                             

$stateProvider.state('biblequotespagecreate', {
          url: "/biblequotespagecreate",
          templateUrl: "templates/biblequotespagecreate.html",
          controller: 'biblequotespagecreateCtrl'
                                })

$stateProvider.state('biblequotespage', {
           url: "/biblequotespage",
           templateUrl: "templates/biblequotespage.html",
           controller: 'biblequotespageCtrl'
                                })  
  $stateProvider.state('biblequotesaddpage', {
          url: "/biblequotesaddpage",
          templateUrl: "templates/biblequotesaddpage.html",
          controller: 'biblequotesaddpageCtrl'
                                })                                                         
  $stateProvider.state('biblequoteseditpage', {
           url: "/biblequoteseditpage",
           templateUrl: "templates/biblequoteseditpage.html",
           controller: 'biblequoteseditpageCtrl'
                                })                                                           
  $stateProvider.state('imageswithdescriptionpagecreate', {
          url: "/imageswithdescriptionpagecreate",
          templateUrl: "templates/imageswithdescriptionpagecreate.html",
          controller: 'imageswithdescriptionpagecreateCtrl'
                                })
  $stateProvider.state('imageswithdescriptionlistpage', {
          url: "/imageswithdescriptionlistpage",
          templateUrl: "templates/imageswithdescriptionlistpage.html",
          controller: 'imageswithdescriptionlistpageCtrl'
                                })
           
  $stateProvider.state('imageswithdescriptionlistaddpage', {
          url: "/imageswithdescriptionlistaddpage",
          templateUrl: "templates/imageswithdescriptionlistaddpage.html",
          controller: 'imageswithdescriptionlistpageaddCtrl'
                                })
           
 $stateProvider.state('imageswithdescriptionlisteditpage', {
          url: "/imageswithdescriptionlisteditpage",
          templateUrl: "templates/imageswithdescriptionlisteditpage.html",
          controller: 'imageswithdescriptionlistpageeditCtrl'
                                })
 $stateProvider.state('imagespagecreate', {
          url: "/imagespagecreate",
          templateUrl: "templates/imagespagecreate.html",
          controller: 'imagespagecreateCtrl'
                                })
 $stateProvider.state('imagespage', {
         url: "/imagespage",
         templateUrl: "templates/imagespage.html",
         controller: 'imagespageCtrl'
                                })
$stateProvider.state('imagesaddpage', {
       url: "/imagesaddpage",
       templateUrl: "templates/imagesaddpage.html",
       controller: 'imagesaddpageCtrl'
                                })
$stateProvider.state('imageseditpage', {
       url: "/imageseditpage",
       templateUrl: "templates/imageseditpage.html",
       controller: 'imageseditpageCtrl'
                                })
           
 $stateProvider.state('videowithdescriptionpagecreate', {
       url: "/videowithdescriptionpagecreate",
       templateUrl: "templates/videowithdescriptionpagecreate.html",
       controller: 'videowithdescriptionpagecreateCtrl'
                                })
$stateProvider.state('videowithdescriptionpage', {
      url: "/videowithdescriptionpage",
      templateUrl: "templates/videowithdescriptionpage.html",
      controller: 'videowithdescriptionpageCtrl'
                                })
$stateProvider.state('videowithdescriptioneditpage', {
      url: "/videowithdescriptioneditpage",
      templateUrl: "templates/videowithdescriptioneditpage.html",
      controller: 'videowithdescriptioneditpageCtrl'
                                })                                
$stateProvider.state('appcreate', {
      url: "/appcreate",
      templateUrl: "templates/appcreate.html",
      controller: 'appcreateCtrl'
                                }) 
$stateProvider.state('donationpage', {
      url: "/donationpage",
      templateUrl: "templates/donationpage.html",
      controller: 'donationpageCtrl'
                                })                                                                  
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/home');
});
