import {Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-google-map',
  templateUrl: './google-map.component.html',
  styleUrls: ['./google-map.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class GoogleMapComponent implements OnInit {
  lat = 21.1591857;
  lng = 72.7522563;
  latA = 21.7613308;
  lngA = 71.6753074;
  zoom = 8;

  styles: any = [{
    featureType: 'all',
    stylers: [{
      saturation: -80
    }]
  }, {
    featureType: 'road.arterial',
    elementType: 'geometry',
    stylers: [{
      hue: '#00ffee'
    }, {
      saturation: 50
    }]
  }, {
    featureType: 'poi.business',
    elementType: 'labels',
    stylers: [{
      visibility: 'off'
    }]
  }];

  constructor() { }

  ngOnInit() {
  }

}
