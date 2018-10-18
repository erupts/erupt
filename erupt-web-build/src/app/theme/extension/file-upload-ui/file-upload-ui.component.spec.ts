import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FileUploadUiComponent } from './file-upload-ui.component';

describe('FileUploadUiComponent', () => {
  let component: FileUploadUiComponent;
  let fixture: ComponentFixture<FileUploadUiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FileUploadUiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileUploadUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
