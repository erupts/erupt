import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreLoaderComponent } from './pre-loader.component';
import {PreLoaderRoutingModule} from './pre-loader-routing.module';

@NgModule({
  imports: [
    CommonModule,
    PreLoaderRoutingModule
  ],
  declarations: [PreLoaderComponent]
})
export class PreLoaderModule { }
