import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OfflineUiComponent } from './offline-ui.component';

describe('OfflineUiComponent', () => {
  let component: OfflineUiComponent;
  let fixture: ComponentFixture<OfflineUiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OfflineUiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OfflineUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
