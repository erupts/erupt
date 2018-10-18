
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {GoogleMapComponent} from './google-map.component';

const routes: Routes = [
  {
    path: '',
    component: GoogleMapComponent,
    data: {
      title: 'Google Map',
      icon: 'icon-map-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - google map',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GoogleMapRoutingModule { }
