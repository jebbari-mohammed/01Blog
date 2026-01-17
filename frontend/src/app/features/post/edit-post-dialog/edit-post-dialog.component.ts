import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { Post } from '../../../core/models/post.model';
import { PostService } from '../../../core/services/post.service';

@Component({
  selector: 'app-edit-post-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './edit-post-dialog.component.html',
  styleUrl: './edit-post-dialog.component.scss'
})
export class EditPostDialogComponent implements OnInit {
  editForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';

  categories = [
    'TECHNOLOGY',
    'LIFESTYLE',
    'TRAVEL',
    'FOOD',
    'HEALTH',
    'EDUCATION',
    'ENTERTAINMENT',
    'SPORTS',
    'BUSINESS',
    'OTHER'
  ];

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private dialogRef: MatDialogRef<EditPostDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { post: Post }
  ) {}

  ngOnInit(): void {
    this.editForm = this.fb.group({
      title: [this.data.post.title, [Validators.required, Validators.maxLength(200)]],
      content: [this.data.post.content, [Validators.required, Validators.maxLength(5000)]],
      category: [this.data.post.category, Validators.required]
    });
  }

  onSubmit(): void {
    if (this.editForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.errorMessage = '';

      this.postService.updatePost(this.data.post.id, this.editForm.value).subscribe({
        next: (updatedPost) => {
          this.dialogRef.close(updatedPost); // Pass updated post back
        },
        error: (error) => {
          console.error('Error updating post:', error);
          this.errorMessage = error.error?.error || 'Failed to update post';
          this.isSubmitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
