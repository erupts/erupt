import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AdminComponent} from "./layout/admin/admin.component";
import {AuthComponent} from "./layout/auth/auth.component";
import {BreadcrumbsComponent} from "./layout/admin/breadcrumbs/breadcrumbs.component";
import {BrowserAnimationsModule, NoopAnimationsModule} from "@angular/platform-browser/animations";
import {SharedModule} from "./shared/shared.module";
import {MenuItems} from "./shared/menu-items/menu-items";
import {MatRippleModule, MatTabsModule} from "@angular/material";
import {AuthModule} from "./auth/auth.module";
import {HashLocationStrategy, LocationStrategy} from "@angular/common";

@NgModule({
  declarations: [
    AppComponent,
    AdminComponent,
    AuthComponent,
    BreadcrumbsComponent
  ],
  imports: [
    BrowserAnimationsModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    AuthModule,
    SharedModule,
    MatTabsModule,
    MatRippleModule
  ],
  exports: [
    BrowserAnimationsModule
  ],
  providers: [MenuItems, {provide: LocationStrategy, useClass: HashLocationStrategy}],
  bootstrap: [AppComponent]
})
export class AppModule {
}
