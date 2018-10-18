import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SizingComponent } from './sizing.component';

describe('SizingComponent', () => {
  let component: SizingComponent;
  let fixture: ComponentFixture<SizingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SizingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SizingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
