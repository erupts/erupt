import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from './navigation.component';
import {NavigationRoutingModule} from './navigation-routing.module';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    NavigationRoutingModule,
    SharedModule
  ],
  declarations: [NavigationComponent]
})
export class NavigationModule { }
